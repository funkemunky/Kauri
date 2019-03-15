package cc.funkemunky.anticheat.api.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Verbose {
    public TickTimer lastFlag = new TickTimer(10);
    private int verbose = 0;

    public boolean flag(int amount) {
        lastFlag.reset();
        return (verbose++) > amount;
    }

    public boolean flagB(int amount, int toAdd) {
        lastFlag.reset();
        return (verbose += toAdd) > amount;
    }

    public boolean flag(int amount, long reset) {
        if (lastFlag.hasNotPassed((int) reset / 50)) {
            lastFlag.reset();
            return (verbose++) > amount;
        }
        verbose = 0;
        lastFlag.reset();
        return false;
    }


    public void deduct() {
        verbose = verbose > 0 ? verbose - 1 : 0;
    }

    public void deduct(int amount) {
        verbose = verbose > 0 ? verbose - amount : 0;
    }

    public boolean flag(int amount, long reset, int toAdd) {
        if (lastFlag.hasNotPassed((int) reset / 50)) {
            lastFlag.reset();
            return (verbose += toAdd) > amount;
        }
        verbose = 0;
        lastFlag.reset();
        return false;
    }
}
