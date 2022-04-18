package eu.hansolo.crac4;

//import jdk.crac.*;


import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * The GenericCache is an implementation of the Cache interface which
 * keeps the keys of type K with it's value of type V in a ConcurrentHashMap.
 * Everytime a value key with it's value is added to the cache, a CacheValue
 * object will be created that contains the value and the point in time where
 * this value will be outdated. The max age for the values can be defined
 * in the constructor cacheTimeout parameter (in seconds).
 * Everytime a key is read from the map, it's expiration time will be extended
 * by the value given by cacheTimeout.
 * With this approach values that will be read more often will stay in the cache
 * where values that are not read within the cacheTimeout will be removed from the
 * cache.
 * @param <K> Key
 * @param <V> Value to cache for the key
 */
public class GenericCache<K, V> implements /*Resource,*/ Cache<K, V> {
    public    static final long                     DEFAULT_CACHE_DELAY   = 30;
    public    static final long                     DEFAULT_CACHE_TIMEOUT = 60;
    private   static final int                      INTERVAL              = 1;
    protected              Map<K, CacheValue<V>>    map;
    protected              long                     cacheTimeout;
    private                long                     checkpointAt;
    private                Runnable                 task;
    private                ScheduledExecutorService executorService;


    // ******************** Constructors **************************************
    public GenericCache() {
        this(DEFAULT_CACHE_DELAY, DEFAULT_CACHE_TIMEOUT);
    }
    public GenericCache(final long initialDelay, final long cacheTimeout) {
        this.task            = () -> clean();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.cacheTimeout    = cacheTimeout;
        this.clear();

        // Register this class as resource in the global context of CRaC
        //Core.getGlobalContext().register(GenericCache.this);

        // Start the executor service that calls clean() every second
        this.executorService.scheduleAtFixedRate(task, initialDelay, INTERVAL, TimeUnit.SECONDS);
    }


    /* ******************** CRaC Methods **************************************
    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        System.out.println("beforeCheckpoint() called in GenericCache");
        checkpointAt = Instant.now().getEpochSecond();
        // Free resources or stop services
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        executorService = null;
    }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("afterRestore() called in GenericCache");
        /*
        * Take pause time into account for cached values
        * Important because otherwise with the next call to clean() all values
        * will be outdated and the cache will be completely empty
        */
        /*
        long delta = Instant.now().getEpochSecond() - checkpointAt;
        map.entrySet().forEach(entry -> entry.getValue().setOutdatedAt(entry.getValue().outdatedAt + delta));

        // Restore resources or re-start services
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(task, 0, INTERVAL, TimeUnit.SECONDS);
    }
    */


    // ******************** Cache Methods *************************************
    @Override public boolean containsKey(final K key) { return this.map.containsKey(key); }

    @Override public Optional<V> get(final K key) { return Optional.ofNullable(this.map.get(key)).map(CacheValue::getValue); }

    @Override public void put(final K key, final V value) { this.map.put(key, this.createCacheValue(value)); }

    @Override public void remove(final K key) { this.map.remove(key); }

    @Override public void clear() { this.map = new ConcurrentHashMap<>(); }

    @Override public void clean() { getExpiredKeys().forEach(key -> remove(key)); }

    public int size() { return map.size(); }

    protected Set<K> getExpiredKeys() {
        return this.map.keySet().parallelStream()
                       .filter(this::isExpired)
                       .collect(Collectors.toSet());
    }

    protected boolean isExpired(final K key) { return Instant.now().getEpochSecond() > map.get(key).getOutdatedAt(); }

    protected CacheValue<V> createCacheValue(final V value) { return new CacheValue<>(value, Instant.now().getEpochSecond() + cacheTimeout); }


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
            outdatedAt += cacheTimeout;
            return value;
        }
        public void setValue(final V value) { this.value = value; }

        public long getOutdatedAt() { return  outdatedAt; }
        public void setOutdatedAt(final long outdatedAt) { this.outdatedAt = outdatedAt; }
    }
}
