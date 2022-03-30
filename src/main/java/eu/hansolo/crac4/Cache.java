package eu.hansolo.crac4;

import java.util.Optional;


public interface Cache<K, V> {
    void clean();

    void clear();

    boolean containsKey(K key);

    Optional<V> get(K key);

    void put(K key, V value);

    void remove(K key);
}
