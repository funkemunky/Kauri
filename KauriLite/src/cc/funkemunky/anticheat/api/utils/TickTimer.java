package cc.funkemunky.anticheat.api.utils;


public class TickTimer {
    private int ticks = Kauri.getInstance().getCurrentTicks(), defaultPassed;

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
