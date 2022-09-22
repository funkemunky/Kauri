package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Block (F)", description = "Checks for safewalk like movement.", executable = true,
        checkType = CheckType.BLOCK, punishVL = 5, devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.PLACE)
public class BlockF extends Check {

    public final MaxDouble verbose = new MaxDouble(5);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (!packet.isPos()) return;

        final double deltaXZ = data.playerInfo.deltaXZ;
        final double lDeltaXZ = data.playerInfo.lDeltaXZ;
        final double accelXZ = Math.abs(deltaXZ - lDeltaXZ);

        if (deltaXZ < 0.15D
                && deltaXZ > 0.1D
                && lDeltaXZ > 0.15D
                && accelXZ < 0.1
                && accelXZ > 0.099
                && data.playerInfo.groundTicks < 18
                && data.playerInfo.serverGround
                && data.playerInfo.lastBlockPlace.isNotPassed(15)) {

            if (verbose.add() > 2) {
                vl++;
                flag("DeltaXZ=%s lDeltaXZ=%s accel=%s", deltaXZ, lDeltaXZ, accelXZ);
            }
        } else {
            verbose.subtract(0.1);
        }

        debug("DeltaXZ=%s accel=%s verbose=%s", deltaXZ, accelXZ, verbose.value());
    }

}
