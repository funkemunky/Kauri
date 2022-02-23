package dev.brighten.anticheat.utils.timer.impl;

import cc.funkemunky.api.Atlas;
import dev.brighten.anticheat.utils.timer.Timer;

public class AtlasTimer implements Timer {

    private long currentStamp, defaultPassed;
    private int resetStreak;

    public AtlasTimer(long defaultPassed) {
        this.defaultPassed = defaultPassed;
    }

    public AtlasTimer() {
        this.defaultPassed = 20;
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
        return Atlas.getInstance().getCurrentTicks() == currentStamp;
    }

    @Override
    public int getResetStreak() {
        return resetStreak;
    }

    @Override
    public long getPassed() {
        return Atlas.getInstance().getCurrentTicks() - currentStamp;
    }

    @Override
    public long getCurrent() {
        return currentStamp;
    }

    @Override
    public void reset() {
        if(getPassed() <= 1) resetStreak++;
        else resetStreak = 0;

        currentStamp = Atlas.getInstance().getCurrentTicks();
    }
}
