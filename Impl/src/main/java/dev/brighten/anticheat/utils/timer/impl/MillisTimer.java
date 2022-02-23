package dev.brighten.anticheat.utils.timer.impl;

import dev.brighten.anticheat.utils.timer.Timer;

public class MillisTimer implements Timer {

    private long currentStamp;
    private int resetStreak;
    private final long defaultPassed;

    public MillisTimer(long defaultPassed) {
        this.defaultPassed = defaultPassed;
    }

    public MillisTimer() {
        defaultPassed = 1000L;
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
        return getPassed() <= 1L;
    }

    @Override
    public int getResetStreak() {
        return resetStreak;
    }

    @Override
    public long getPassed() {
        return System.currentTimeMillis() - currentStamp;
    }

    @Override
    public long getCurrent() {
        return currentStamp;
    }

    @Override
    public void reset() {
        if(getPassed() <= 60L) resetStreak++;
        else resetStreak = 0;

        currentStamp = System.currentTimeMillis();
    }
}
