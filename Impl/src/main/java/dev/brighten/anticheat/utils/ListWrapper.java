package dev.brighten.anticheat.utils;

import java.util.List;

public abstract class ListWrapper<T> implements List<T> {
    protected final List<T> base;

    public ListWrapper(List<T> base) {
        this.base = base;
    }

    public List<T> getBase() {
        return this.base;
    }
}