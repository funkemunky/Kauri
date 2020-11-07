package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (B)", description = "Checks for changing in air which should be impossible.",
        checkType = CheckType.SPEED, punishVL = 20, vlToFlag = 2)
@Cancellable
public class SpeedB extends Check {

    private float verbose;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()
                && !data.playerInfo.generalCancel) {
            if(data.playerInfo.airTicks > 2 && !data.playerInfo.lClientGround && !data.playerInfo.clientGround) {
                double accelX = data.playerInfo.deltaX - data.playerInfo.lDeltaX;
                double accelZ = data.playerInfo.deltaZ - data.playerInfo.lDeltaZ;
                double hypot = MathUtils.hypot(accelX, accelZ);

                if(hypot > 0.1
                        && !data.blockInfo.blocksNear
                        && data.playerInfo.lastVelocity.isPassed(8)
                        && !data.blockInfo.inLiquid
                        && data.playerInfo.lastHalfBlock.isPassed(10)
                        && (accelX > -0.07 || accelZ > -0.07)
                        && data.playerInfo.lastBlockPlace.isPassed(7)) {
                    if(verbose++ > 2) {
                        vl++;
                        flag("x=%v z=%v",
                                MathUtils.round(accelX, 3), MathUtils.round(accelZ, 3));
                    }
                } else verbose-= verbose > 0 ? 0.2f : 0;

                debug("x=" + accelX + " z=" + accelZ + " vl=" + verbose);
            } else if(verbose > 0) verbose-= 0.1f;
        } else if(verbose > 0) verbose-= 0.05f;
    }
}