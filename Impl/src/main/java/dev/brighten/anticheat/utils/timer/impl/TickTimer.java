package dev.brighten.anticheat.utils.timer.impl;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.utils.timer.Timer;

public class TickTimer implements Timer {
    private long currentStamp;
    private int resetStreak;
    private final long defaultPassed;

    public TickTimer(long defaultPassed) {
        this.defaultPassed = defaultPassed;
        currentStamp = Kauri.INSTANCE.keepaliveProcessor.tick;
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
    public int getResetStreak() {
        return resetStreak;
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
        if(getPassed() <= 1) resetStreak++;
        else resetStreak = 0;

        currentStamp = Kauri.INSTANCE.keepaliveProcessor.tick;
    }
}
