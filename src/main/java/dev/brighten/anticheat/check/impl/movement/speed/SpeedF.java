package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (F)", description = "Checks for very consistent acceleration on the x and z axis.",
        checkType = CheckType.SPEED, punishVL = 20)
public class SpeedF extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float accel = Math.abs(data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ);

            if(data.playerInfo.clientGround) return;

            if(!data.playerInfo.generalCancel
                    && (data.playerInfo.deltaXZ > 0.1 || data.playerInfo.lDeltaXZ > 0.1)
                    && accel < 0.00001) {
                if(vl++ > 5) {
                    flag("accel=" + accel);
                }
            } else vl-= vl > 0 ? 0.5 : 0;

            debug("accel=" + accel + " vl=" + vl);
        }
    }
}
