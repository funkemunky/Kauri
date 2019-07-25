package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.api.utils.TickTimer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Verbose {
    public TickTimer lastFlag = new TickTimer(10);
    private double verbose = 0;

    public boolean flag(int amount) {
        lastFlag.reset();
        return (verbose++) > amount;
    }

    public boolean flagB(double amount, int toAdd) {
        lastFlag.reset();
        return (verbose += toAdd) > amount;
    }

    public boolean flag(double amount, long reset) {
        return flag(amount, reset, 1);
    }


    public void deduct() {
        deduct(1);
    }

    public void deduct(double amount) {
        verbose = verbose > 0 ? verbose - amount : 0;
    }

    public boolean flag(double amount, long reset, int toAdd) {
        if (lastFlag.hasNotPassed((int) reset / 50)) {
            lastFlag.reset();
            return (verbose += toAdd) > amount;
        }
        verbose = 0;
        lastFlag.reset();
        return false;
    }
}
