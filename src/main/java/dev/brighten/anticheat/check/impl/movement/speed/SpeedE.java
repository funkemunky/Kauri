package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (E)", description = "Checks for changing in air which should be impossible.",
        checkType = CheckType.SPEED, punishVL = 20)
public class SpeedE extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()
                && !data.playerInfo.generalCancel
                && timeStamp - data.playerInfo.lastVelocityTimestamp > 250L) {
            if(data.playerInfo.airTicks > 2) {
                float accelX = MathUtils.getDelta(data.playerInfo.deltaX, data.playerInfo.lDeltaX);
                float accelZ = MathUtils.getDelta(data.playerInfo.deltaZ, data.playerInfo.lDeltaZ);
                float hypot = MathUtils.hypot(accelX, accelZ);

                if(hypot > 0.2 && data.playerInfo.lastBlockPlace.hasPassed(10)) {
                    if(vl++ > 4) {
                        flag("x=" + accelX + " z=" + accelZ);
                    }
                } else vl-= vl > 0 ? 0.1 : 0;

                debug("x=" + accelX + " z=" + accelZ + " vl=" + vl);
            }
        }
    }
}
