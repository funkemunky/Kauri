/*
 * Copyright (c) 2018 NGXDEV.COM. Licensed under MIT.
 */

package cc.funkemunky.anticheat.api.utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.stream.Stream;

public class EvictingList<T> extends ArrayDeque<T> {
    private int maxSize;

    public EvictingList(int maxSize) {
        this.maxSize = maxSize;
    }

    public EvictingList(Collection<? extends T> c, int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean add(T t) {
        if (size() >= maxSize) remove(0);
        return super.add(t);
    }

    @Override
    public Stream<T> stream() {
        return new ArrayDeque<>(this).stream();
    }
}