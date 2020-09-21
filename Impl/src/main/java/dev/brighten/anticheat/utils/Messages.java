package dev.brighten.anticheat.utils;

import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Messages {

    EMPTY(() -> "");

    public final Supplier<String> message;
}
