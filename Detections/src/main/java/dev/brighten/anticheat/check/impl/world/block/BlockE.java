package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Block (E)", description = "Checks for invalid block place motions.", executable = true,
        checkType = CheckType.BLOCK, punishVL = 5, devStage = DevStage.ALPHA)
@Cancellable(cancelType = CancelType.PLACE)
public class BlockE extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if (!packet.isPos() || !packet.isLook()) return;
        final double deltaXZ = data.playerInfo.deltaXZ;
        final double accelXZ = Math.abs(deltaXZ - data.playerInfo.lDeltaXZ);

        if (deltaXZ > 0.15D
                && accelXZ < 1.0E-5
                && (data.playerInfo.deltaYaw > 1 || data.playerInfo.deltaPitch > 1)
                && data.playerInfo.groundTicks < 18
                && data.playerInfo.serverGround
                && data.playerInfo.lastBlockPlace.isNotPassed(15)) {
            vl++;
            flag("deltaXZ=%s accelXZ=%s", deltaXZ, accelXZ);
        }

        debug("deltaXZ=%s accelXZ=%s", deltaXZ, accelXZ);
    }

}
