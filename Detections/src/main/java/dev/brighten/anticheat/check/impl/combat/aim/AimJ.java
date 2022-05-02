package dev.brighten.anticheat.check.impl.combat.aim;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (J)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, devStage = DevStage.ALPHA)
public class AimJ extends Check {
    private int buffer;
    public void runCheck(double std, double pstd, double[] offset, float[] rot) {
        double min = Math.min(std, pstd);
        double pct = Math.abs(std - pstd) / min * 100;

        if(pct > 200 && min > 1.4 && std < 40) {
            if(data.playerInfo.deltaYaw > 0.3 && ++buffer > 8) {
                vl++;
                flag("t=c pct=%.1f%%", pct);
                buffer = 8;
            }
        } else buffer = 0;

        debug("[%s] pct=%.1f%% std=%.4f pstd=%.4f m=%.2f y=%.1f p=%.1f", buffer, pct, std, pstd, min, offset[0], offset[1]);
    }
}
