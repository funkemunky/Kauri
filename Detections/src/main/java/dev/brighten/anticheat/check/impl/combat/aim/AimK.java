package dev.brighten.anticheat.check.impl.combat.aim;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (K)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, devStage = DevStage.ALPHA)
public class AimK extends Check {

    private int buffer;

    public void runCheck(double std, double pstd, double[] offset, float[] rot) {
        if(std < 1) {
            if(data.playerInfo.deltaYaw > 0.2 && ++buffer > 8) {
                vl++;
                buffer = 8;
                flag("t=b y=%.2f dy=%.3f s=%.3f", offset[1], data.playerInfo.deltaYaw, std);
            }
        } else buffer = 0;

        debug("%.3f,%s", std, buffer);
    }
}
