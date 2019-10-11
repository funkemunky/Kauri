package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Speed (C)", description = "Checks for instant changes in movement.", punishVL = 4)
public class SpeedC extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()
                && !data.playerInfo.generalCancel
                && data.playerInfo.lastVelocity.hasPassed(5 + MathUtils.millisToTicks(data.lagInfo.ping))) {
            float baseSpeed = MovementUtils.getBaseSpeed(data) + (data.playerInfo.serverGround ? 0.35f : 0.5f);

            if(data.blockInfo.blocksAbove || data.playerInfo.iceTicks > 0) baseSpeed+= baseSpeed * 0.5 + 0.5;

            if(data.playerInfo.deltaXZ > baseSpeed) {
                if(vl++ > 1 || (data.playerInfo.deltaXZ - baseSpeed) > 1.2)
                    flag(data.playerInfo.deltaXZ + ">-" + baseSpeed);
            } else vl-= vl > 0 ? 0.1 : 0;
            debug("base=" + baseSpeed + " deltaXZ=" + data.playerInfo.deltaXZ);
        }
    }
}