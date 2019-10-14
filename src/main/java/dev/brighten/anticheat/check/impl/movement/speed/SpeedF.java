package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (F)", description = "Checks for very consistent acceleration on the x and z axis.",
        checkType = CheckType.SPEED, punishVL = 20)
public class SpeedF extends Check {

    private float lastAccel;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            float accel = Math.abs(data.playerInfo.deltaXZ - data.playerInfo.lDeltaXZ);

            if(!data.playerInfo.clientGround && MathUtils.getDelta(accel, lastAccel) < 0.001) {
                if(vl++ > 5) {
                    flag("accel=" + accel + " lAccel=" + lastAccel);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            lastAccel = accel;
        }
    }
}
