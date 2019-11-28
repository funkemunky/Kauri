package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Speed (A)", description = "A simple limiting speed check with a high verbose threshold.",
        punishVL = 40)
public class SpeedA extends Check {

    private long moveTicks, keyTicks;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel || data.playerInfo.lastVelocity.hasNotPassed(25))
            return;

        float baseSpeed = MovementUtils.getBaseSpeed(data) + (!data.playerInfo.serverGround ? 0.09f : 0.06f);

        baseSpeed+= data.playerInfo.iceTicks > 0 ? 0.4 + (Math.min(120, data.playerInfo.iceTicks) * 0.01) : 0;
        baseSpeed+= data.playerInfo.blocksAboveTicks > 0 ? 0.35
                + (Math.min(60, data.playerInfo.blocksAboveTicks) * 0.005) : 0;
        baseSpeed+= data.playerInfo.halfBlockTicks > 0 ? 0.2
                + (Math.min(40, data.playerInfo.halfBlockTicks)) * 0.005 : 0;
        baseSpeed+= data.playerInfo.wasOnSlime ? 0.1 : 0;

        if(data.playerInfo.deltaXZ > baseSpeed) {
            if(vl++ > 25) flag(data.playerInfo.deltaXZ + ">-" + baseSpeed);
        } else vl-= vl > 0 ? 1 : 0;

        debug("deltaXZ=" + data.playerInfo.deltaXZ + " baseSpeed=" + baseSpeed + " vl=" + vl
                + " onSlime=" + data.playerInfo.wasOnSlime);
    }
}
