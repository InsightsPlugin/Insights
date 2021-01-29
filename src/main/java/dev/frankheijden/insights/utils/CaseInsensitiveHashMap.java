package dev.frankheijden.insights.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CaseInsensitiveHashMap<V> extends HashMap<String, V> {

    public CaseInsensitiveHashMap() {
        super();
    }

    public CaseInsensitiveHashMap(Map<? extends String, ? extends V> m) {
        super(m.size());
        this.putAll(m);
    }

    public CaseInsensitiveHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CaseInsensitiveHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public V get(Object key) {
        return super.get(key.toString().toUpperCase());
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key.toString().toUpperCase(), defaultValue);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key.toString().toUpperCase());
    }

    @Override
    public V merge(String key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(key.toUpperCase(), value, remappingFunction);
    }

    @Override
    public V put(String key, V value) {
        return super.put(key.toUpperCase(), value);
    }

    @Override
    public V putIfAbsent(String key, V value) {
        return super.putIfAbsent(key.toUpperCase(), value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        for (Entry<? extends String, ? extends V> e : m.entrySet()) {
            put(e.getKey().toUpperCase(), e.getValue());
        }
    }

    @Override
    public V compute(String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
        return super.compute(key.toUpperCase(), remappingFunction);
    }

    @Override
    public V computeIfAbsent(String key, Function<? super String, ? extends V> mappingFunction) {
        return super.computeIfAbsent(key.toUpperCase(), mappingFunction);
    }

    @Override
    public V computeIfPresent(String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(key.toUpperCase(), remappingFunction);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(key.toString().toUpperCase(), value);
    }

    @Override
    public V remove(Object key) {
        return super.remove(key.toString().toUpperCase());
    }

    @Override
    public boolean replace(String key, V oldValue, V newValue) {
        return super.replace(key.toUpperCase(), oldValue, newValue);
    }

    @Override
    public V replace(String key, V value) {
        return super.replace(key.toUpperCase(), value);
    }
}
