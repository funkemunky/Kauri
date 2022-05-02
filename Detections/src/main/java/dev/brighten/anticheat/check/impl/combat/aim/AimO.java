package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.SimpleAverage;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Aim (O)", description = "Statistical aim analysis",
        checkType = CheckType.AIM, devStage = DevStage.ALPHA)
public class AimO extends Check {

    private int buffer;
    private final SimpleAverage angleAverage = new SimpleAverage(15, 0);

    public void runCheck(double std, double pstd, double[] offset, float[] rot) {
        double deltaXAvg = angleAverage.getAverage();

        double weightedStd = std / Math.max(0.01, Math.min(3.8, (deltaXAvg * 0.04)));

        if(weightedStd < 1 && std > 0 && deltaXAvg > 8) {
            if(++buffer > 8) {
                buffer = 8;
                vl++;
                flag("w=%.3f std=%.1f avg=%.1f", weightedStd, std, deltaXAvg);
            }
        } else if(buffer > 0) buffer-= 3;
        debug("w=%.3f std=%.1f b=%s avg=%.1f", weightedStd, std, buffer, deltaXAvg);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        angleAverage.add(Math.abs(data.moveProcessor.deltaX));
    }
}
