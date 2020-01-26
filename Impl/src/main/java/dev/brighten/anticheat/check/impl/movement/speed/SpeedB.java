package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (B)", description = "Checks for changing in air which should be impossible.",
        checkType = CheckType.SPEED, punishVL = 20)
@Cancellable
public class SpeedB extends Check {

    private float verbose;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()
                && !data.playerInfo.generalCancel
                && timeStamp - data.playerInfo.lastVelocityTimestamp > 250L) {
            if(data.playerInfo.airTicks > 2) {
                double accelX = data.playerInfo.deltaX - data.playerInfo.lDeltaX;
                double accelZ = data.playerInfo.deltaZ - data.playerInfo.lDeltaZ;
                double hypot = MathUtils.hypot(accelX, accelZ);

                if(hypot > 0.12
                        && !data.blockInfo.blocksNear
                        && !data.blockInfo.inLiquid
                        && data.playerInfo.lastHalfBlock.hasPassed(4)
                        && (accelX > -0.07 || accelZ > -0.07)
                        && data.playerInfo.lastBlockPlace.hasPassed(10)) {
                    if(verbose++ > 2) {
                        vl++;
                        flag("x=" + accelX + " z=" + accelZ);
                    }
                } else verbose-= verbose > 0 ? 0.1 : 0;

                debug("x=" + accelX + " z=" + accelZ + " vl=" + vl);
            }
        }
    }
}