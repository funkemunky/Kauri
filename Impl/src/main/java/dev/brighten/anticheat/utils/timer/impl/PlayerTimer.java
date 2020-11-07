package dev.brighten.anticheat.utils.timer.impl;

import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.timer.Timer;

public class PlayerTimer implements Timer {

    private long currentTick;
    private final long defaultPassed;
    private final ObjectData data;

    public PlayerTimer(ObjectData data) {
        this.data = data;
        this.defaultPassed = 20;
    }

    public PlayerTimer(ObjectData data, long defaultPassed) {
        this.defaultPassed = defaultPassed;
        this.data = data;
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
        return data.playerTicks == currentTick;
    }

    @Override
    public long getPassed() {
        return data.playerTicks - currentTick;
    }

    @Override
    public long getCurrent() {
        return currentTick;
    }

    @Override
    public void reset() {
        currentTick = data.playerTicks;
    }
}
