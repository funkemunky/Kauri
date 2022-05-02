package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.utils.math.RollingAverageDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (L)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, devStage = DevStage.ALPHA)
public class AimL extends Check {

    private int buffer;
    private float lrp;
    private double lstd, lpstd;
    private final RollingAverageDouble stdDelta = new RollingAverageDouble(3, 0);

    public void runCheck(double std, double pstd, double[] offset, float[] rot) {
        double deltaRot = rot[1] - lrp;
        stdDelta.add(Math.abs(pstd - lpstd) + Math.abs(std - lstd));

        double avgstdDelta = stdDelta.getAverage();

        if(((deltaRot < 0 && data.playerInfo.deltaPitch < 0) || (deltaRot > 0 && data.playerInfo.deltaPitch > 0))
                && (Math.abs(data.playerInfo.deltaYaw) > 0.3 || data.playerInfo.deltaXZ > 0.28)
                && avgstdDelta < 3 && pstd < 11) {
            if(++buffer > 14) {
                vl++;
                flag("t=d");
                buffer = Math.min(16, buffer);
            }
        } else if(buffer > 0) buffer-= 2;
        debug("[%.3f] b=%s po=%.4f pr=%.4f p=%.4f x=%.3f y=%.3f", avgstdDelta, buffer, offset[1],
                data.playerInfo.deltaPitch, deltaRot, std, pstd);

        lstd = std;
        lpstd = pstd;
        lrp = rot[1];
    }
}
