package dev.brighten.anticheat.utils;

import dev.brighten.anticheat.Kauri;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class CacheList<E> extends CopyOnWriteArrayList<E> {

    private final Map<E, Long> cachedTimes = new ConcurrentHashMap<>();
    private final long expireTime;

    public CacheList(long millis) {
        this.expireTime = millis;
        runTask();
    }

    public CacheList(long time, TimeUnit unit) {
        this.expireTime = unit.toMillis(time);
        runTask();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::updateValue);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        c.forEach(this::updateValue);
        return super.addAll(index, c);
    }

    @Override
    public boolean add(E e) {
        updateValue(e);
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        updateValue(element);
        super.add(index, element);
    }

    @Override
    public E set(int index, E element) {
        updateValue(element);
        return super.set(index, element);
    }

    @Override
    public boolean addIfAbsent(E e) {
        if(super.addIfAbsent(e)) {
            updateValue(e);
            return true;
        }
        return false;
    }

    private void runTask() {
        Kauri.INSTANCE.executor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            cachedTimes.forEach((key, time) -> {
                if(!contains(key)) cachedTimes.remove(key);
                else if(now - time > expireTime) {
                    remove(key);
                    cachedTimes.remove(key);
                    System.out.println("Cleared " + key);
                }
            });
        }, 500L, 150L, TimeUnit.MILLISECONDS);
    }

    private void updateValue(E val) {
        this.cachedTimes.put(val, System.currentTimeMillis());
    }
}
