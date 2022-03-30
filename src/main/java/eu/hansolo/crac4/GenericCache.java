package eu.hansolo.crac4;

import jdk.crac.Context;
import jdk.crac.Core;
import jdk.crac.Resource;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class GenericCache<K, V> implements Cache<K, V>, Resource {
    public    static final long                     DEFAULT_CACHE_TIMEOUT = 60;
    private   static final int                      INTERVAL              = 1;
    protected              Map<K, CacheValue<V>>    map;
    protected              long                     timeout;
    private                Runnable                 task;
    private                ScheduledExecutorService executorService;


    // ******************** Constructors **************************************
    public GenericCache() {
        this(DEFAULT_CACHE_TIMEOUT);
    }
    public GenericCache(final long timeout) {
        this.task            = () -> clean();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.timeout         = timeout;
        this.clear();

        this.executorService.scheduleAtFixedRate(task, 10, INTERVAL, TimeUnit.SECONDS);
    }


    // ******************** Methods *******************************************
    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        System.out.println("beforeCheckpoint() called in GenericCache");
        // Shutdown services
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        executorService = null;
    }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("afterRestore() called in GenericCache");
        // Restart services
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(task, 0, INTERVAL, TimeUnit.SECONDS);
    }

    @Override public boolean containsKey(final K key) { return this.map.containsKey(key); }

    @Override public Optional<V> get(final K key) { return Optional.ofNullable(this.map.get(key)).map(CacheValue::getValue); }

    @Override public void put(final K key, final V value) { this.map.put(key, this.createCacheValue(value)); }

    @Override public void remove(final K key) { this.map.remove(key); }

    @Override public void clear()                 { this.map = new ConcurrentHashMap<>(); }

    @Override public void clean() { getExpiredKeys().forEach(key -> remove(key)); }

    public int size() { return map.size(); }

    protected Set<K> getExpiredKeys() {
        return this.map.keySet().parallelStream()
                       .filter(this::isExpired)
                       .collect(Collectors.toSet());
    }

    protected boolean isExpired(final K key) { return Instant.now().getEpochSecond() > map.get(key).getOutdatedAt(); }

    protected CacheValue<V> createCacheValue(final V value) { return new CacheValue<>(value, Instant.now().getEpochSecond() + timeout); }


    // ******************** Internal classes **********************************
    public class CacheValue<V> {
        private V    value;
        private long outdatedAt;


        // ******************** Constructors **********************************
        public CacheValue(final V value, final long outdatedAt) {
            this.value      = value;
            this.outdatedAt = outdatedAt;
        }


        // ******************** Methods ***************************************
        public V getValue() {
            outdatedAt += timeout;
            return value;
        }
        public void setValue(final V value) { this.value = value; }

        public long getOutdatedAt() { return  outdatedAt; }
        public void setOutdatedAt(final long outdatedAt) { this.outdatedAt = outdatedAt; }
    }
}
