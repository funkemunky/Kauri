package dev.brighten.anticheat.check.impl.movement.noslow;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "NoSlow (A)", description = "Checks for invalid web movements.", executable = true,
        checkType = CheckType.NOSLOW, devStage = DevStage.ALPHA)
@Cancellable
public class NoSlowA extends Check {

    private final MaxDouble maxDouble = new MaxDouble(5);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        final double deltaXZ = data.playerInfo.deltaXZ;

        if (deltaXZ > 0.1 && data.playerInfo.inWebTimer.isPassed(20)) {
            if (maxDouble.add() > 2) {
                flag("deltaXZ=%s", deltaXZ);
            }
        } else {
            maxDouble.subtract(0.025);
        }
    }

}
