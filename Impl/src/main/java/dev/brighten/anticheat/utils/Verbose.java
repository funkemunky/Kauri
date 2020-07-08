package dev.brighten.anticheat.utils;

import dev.brighten.anticheat.utils.TickTimer;
import cc.funkemunky.api.utils.math.cond.MaxDouble;

public class Verbose {
    private MaxDouble vl;
    private double maxVl;
    private TickTimer lastFlag;

    public Verbose(double maxVl, int resetTicks) {
        vl = new MaxDouble(this.maxVl = maxVl);
        lastFlag = new TickTimer(resetTicks);
    }

    public boolean flag(double toAdd, double max) {
        if(lastFlag.hasPassed()) {
            vl.subtract(maxVl * 1.5);
        }
        lastFlag.reset();

        return vl.add(toAdd) > max;
    }

    public void subtract(double amount) {
        vl.subtract(amount);
    }

    public double value() {
        return vl.value();
    }
}
