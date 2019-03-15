package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Kauri;


public class TickTimer {
    private int ticks = 0, defaultPassed;

    public TickTimer(int defaultPassed) {
        this.defaultPassed = defaultPassed;
    }

    public void reset() {
        ticks = Kauri.getInstance().getCurrentTicks();
    }

    public boolean hasPassed() {
        return Kauri.getInstance().getCurrentTicks() - ticks > defaultPassed;
    }

    public boolean hasPassed(int amount) {
        return Kauri.getInstance().getCurrentTicks() - ticks > amount;
    }

    public boolean hasNotPassed() {
        return Kauri.getInstance().getCurrentTicks() - ticks <= defaultPassed;
    }

    public boolean hasNotPassed(int amount) {
        return Kauri.getInstance().getCurrentTicks() - ticks <= amount;
    }

    public int getPassed() {
        return Kauri.getInstance().getCurrentTicks() - ticks;
    }
}
