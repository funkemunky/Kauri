package dev.brighten.anticheat.utils.timer.impl;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.utils.timer.Timer;

public class TickTimer implements Timer {
    private long currentStamp;
    private final long defaultPassed;

    public TickTimer(long defaultPassed) {
        this.defaultPassed = defaultPassed;
    }

    public TickTimer() {
        defaultPassed = 20;
    }

    @Override
    public boolean isPassed(long stamp) {
        return getPassed() > stamp;
    }

    @Override
    public boolean isPassed() {
        return getPassed() > defaultPassed;
    }

    @Override
    public boolean isNotPassed(long stamp) {
        return getPassed() <= stamp;
    }

    @Override
    public boolean isNotPassed() {
        return getPassed() <= defaultPassed;
    }

    @Override
    public boolean isReset() {
        return getPassed() == 0;
    }

    @Override
    public long getPassed() {
        return Kauri.INSTANCE.keepaliveProcessor.tick - currentStamp;
    }

    @Override
    public long getCurrent() {
        return currentStamp;
    }

    @Override
    public void reset() {
        currentStamp = Kauri.INSTANCE.keepaliveProcessor.tick;
    }
}
