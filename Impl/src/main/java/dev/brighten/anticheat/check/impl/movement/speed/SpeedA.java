package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Speed (A)", description = "A simple limiting speed check with a high verbose threshold.",
        punishVL = 40)
@Cancellable
public class SpeedA extends Check {

    private MaxInteger verbose = new MaxInteger(40);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel || data.playerInfo.lastVelocity.hasNotPassed(25))
            return;

        float baseSpeed = MovementUtils.getBaseSpeed(data) + (!data.playerInfo.clientGround ? 0.09f
                : (data.playerInfo.groundTicks > 10 ? 0.04f : 0.06f));

        baseSpeed+= data.playerInfo.iceTicks.value() > 0 ? 0.4
                + (Math.min(120, data.playerInfo.iceTicks.value()) * 0.01) : 0;
        baseSpeed+= data.playerInfo.blocksAboveTicks.value() > 0 ? 0.35
                + (data.playerInfo.blocksAboveTicks.value() * 0.005) : 0;
        baseSpeed+= data.playerInfo.halfBlockTicks.value() > 0 ? 0.2
                + data.playerInfo.halfBlockTicks.value() * 0.005 : 0;
        baseSpeed+= data.playerInfo.wasOnSlime ? 0.1 : 0;

        if(data.playerInfo.deltaXZ > baseSpeed) {
            if(verbose.add(data.playerInfo.deltaXZ - baseSpeed > 0.6f ? 4 : 1) > 25
                    || data.playerInfo.deltaXZ - baseSpeed > 0.6f) {
                vl++;
                flag(data.playerInfo.deltaXZ + ">-" + baseSpeed);
            }
        } else verbose.subtract();

        debug("deltaXZ=" + data.playerInfo.deltaXZ + " baseSpeed=" + baseSpeed + " vl=" + vl
                + " onSlime=" + data.playerInfo.wasOnSlime);
    }
}
