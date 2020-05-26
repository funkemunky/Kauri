package dev.brighten.anticheat.utils;

import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TickTimer {
    private final ObjectData data;
    private final int defaultPassed;
    private int ticks;

    public void reset() {
        ticks = data.currentTicks;
    }

    public boolean hasPassed() {
        return data.currentTicks - ticks > defaultPassed;
    }

    public boolean hasPassed(int amount) {
        return data.currentTicks - ticks > amount;
    }

    public boolean hasNotPassed() {
        return data.currentTicks - ticks <= defaultPassed;
    }

    public boolean hasNotPassed(int amount) {
        return data.currentTicks - ticks <= amount;
    }

    public int getPassed() {
        return data.currentTicks - ticks;
    }
}
