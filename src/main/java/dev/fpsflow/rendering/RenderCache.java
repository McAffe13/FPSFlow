package dev.fpsflow.rendering;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple bounded LRU cache used to store pre-computed rendering data.
 * Entries are keyed by an arbitrary long key (e.g. block state id, entity type hash).
 */
public final class RenderCache<V> {

    private final int capacity;
    private final Map<Long, V> map;

    public RenderCache(int capacity) {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, V> eldest) {
                return size() > RenderCache.this.capacity;
            }
        };
    }

    public V get(long key) {
        return map.get(key);
    }

    public void put(long key, V value) {
        map.put(key, value);
    }

    public boolean contains(long key) {
        return map.containsKey(key);
    }

    public void invalidate(long key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }
}
