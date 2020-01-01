package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Speed (C)", description = "Checks for changing in air which should be impossible.",
        checkType = CheckType.SPEED, punishVL = 20)
public class SpeedC extends Check {

    private float verbose;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()
                && !data.playerInfo.generalCancel
                && timeStamp - data.playerInfo.lastVelocityTimestamp > 250L) {
            if(data.playerInfo.airTicks > 2) {
                float accelX = data.playerInfo.deltaX - data.playerInfo.lDeltaX;
                float accelZ = data.playerInfo.deltaZ - data.playerInfo.lDeltaZ;
                float hypot = MathUtils.hypot(accelX, accelZ);

                if(hypot > 0.12
                        && !data.blockInfo.blocksNear
                        && data.playerInfo.halfBlockTicks.value() == 0
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