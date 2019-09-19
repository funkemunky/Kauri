package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Speed (D)", description = "A ground speed check.")
public class SpeedD extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
       float baseSpeed = MovementUtils.getBaseSpeed(data)
               + 0.075f
               + (data.playerInfo.groundTicks < 8 ? 0.4f * (float)Math.pow(0.75f, data.playerInfo.groundTicks) : 0);

       if(data.playerInfo.serverGround
               && data.playerInfo.deltaXZ > baseSpeed
               && !data.playerInfo.generalCancel
               && data.playerInfo.lastVelocity.hasPassed(20 + MathUtils.millisToTicks(data.lagInfo.ping))) {
           if(vl++ > 30) {
               punish();
           } else if(vl > 5) flag(data.playerInfo.deltaXZ + ">-" + baseSpeed);
       } else vl-= vl > 0 ? 0.25 : 0;
    }

}
