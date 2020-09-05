package dev.brighten.anticheat.utils;

import dev.brighten.anticheat.Kauri;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheMap<K, V> extends ConcurrentHashMap<K, V> {

    private final Map<K, Long> cachedTimes = new ConcurrentHashMap<>();
    private final long expireTime;

    public CacheMap(long millis) {
        this.expireTime = millis;
        runTask();
    }

    public CacheMap(long time, TimeUnit unit) {
        this.expireTime = unit.toMillis(time);
        runTask();
    }

    @Override
    public V put(K key, V value) {
        cachedTimes.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((k, v) -> cachedTimes.put(k, System.currentTimeMillis()));
        super.putAll(m);
    }

    private void runTask() {
        Kauri.INSTANCE.executor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            cachedTimes.forEach((key, time) -> {
                if(!containsKey(key)) cachedTimes.remove(key);
                else if(now - time > expireTime) {
                    remove(key);
                    cachedTimes.remove(key);
                }
            });
        }, 500L, 150L, TimeUnit.MILLISECONDS);
    }
}
