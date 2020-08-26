package dev.brighten.anticheat.utils;

import dev.brighten.anticheat.Kauri;

public class TickTimer {
    private int ticks = Kauri.INSTANCE.keepaliveProcessor.tick, defaultPassed;

    public TickTimer(int defaultPassed) {
        this.defaultPassed = defaultPassed;
    }

    public void reset() {
        ticks = Kauri.INSTANCE.keepaliveProcessor.tick;
    }

    public boolean hasPassed() {
        return Kauri.INSTANCE.keepaliveProcessor.tick - ticks > defaultPassed;
    }

    public boolean hasPassed(int amount) {
        return Kauri.INSTANCE.keepaliveProcessor.tick - ticks > amount;
    }

    public boolean hasNotPassed() {
        return Kauri.INSTANCE.keepaliveProcessor.tick - ticks <= defaultPassed;
    }

    public boolean hasNotPassed(int amount) {
        return Kauri.INSTANCE.keepaliveProcessor.tick - ticks <= amount;
    }

    public int getPassed() {
        return Kauri.INSTANCE.keepaliveProcessor.tick - ticks;
    }
}
