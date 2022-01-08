package dev.brighten.api.handlers;

import dev.brighten.api.data.Data;

@FunctionalInterface
public interface ExemptParameter {
    boolean exempt(Data data);
}
