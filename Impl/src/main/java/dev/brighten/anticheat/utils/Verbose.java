package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;

public class Verbose {
    private MaxDouble vl;
    private double maxVl;
    private Timer lastFlag;

    public Verbose(double maxVl, int resetTicks) {
        vl = new MaxDouble(this.maxVl = maxVl);
        lastFlag = new AtlasTimer(resetTicks);
    }

    public boolean flag(double toAdd, double max) {
        if(lastFlag.isPassed()) {
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
