package dev.brighten.anticheat.check.impl.premium;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (L)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, planVersion = KauriVersion.ARA, devStage = DevStage.CANARY)
public class AimL extends Check {

    private int buffer;
    private float lrp;

    public void runCheck(double std, double pstd, double[] offset, float[] rot) {
        double deltaRot = rot[1] - lrp;

        if(((deltaRot < 0 && data.playerInfo.deltaPitch < 0) || (deltaRot > 0 && data.playerInfo.deltaPitch > 0))
                && (data.playerInfo.deltaYaw > 0.3 || data.playerInfo.deltaXZ > 0.28) && pstd < 11) {
            if(++buffer > 8) {
                vl++;
                flag("t=d");
                buffer = 9;
            }
        } else if(buffer > 0) buffer-= 2;
        debug("b=%s po=%.4f pr=%.4f p=%.4f x=%.3f y=%.3f", buffer, offset[1],
                data.playerInfo.deltaPitch, deltaRot, std, pstd);

        lrp = rot[1];
    }
}
