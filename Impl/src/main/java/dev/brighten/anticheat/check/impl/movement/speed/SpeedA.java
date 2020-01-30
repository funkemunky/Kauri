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

        double baseSpeed = MovementUtils.getBaseSpeed(data) + (!data.playerInfo.clientGround ? -0.06f
                : (data.playerInfo.groundTicks > 10 ? -.04 : -.02f));

        baseSpeed+= data.playerInfo.iceTimer.hasNotPassed(70) ? 0.4
                + (Math.min(60, 60 - data.playerInfo.iceTimer.getPassed()) * 0.015) : 0;
        baseSpeed+= data.playerInfo.blockAboveTimer.hasNotPassed(20) ? 0.35
                + ((20 - data.playerInfo.blockAboveTimer.getPassed()) * 0.005) : 0;
        baseSpeed+= data.playerInfo.lastHalfBlock.hasNotPassed(20)
                ? 0.2 + (20 - data.playerInfo.lastHalfBlock.getPassed()) * 0.005
                : 0;
        baseSpeed+= data.playerInfo.wasOnSlime ? 0.1 : 0;

        if(data.playerInfo.deltaXZ > baseSpeed) {
            if(verbose.add(data.playerInfo.deltaXZ - baseSpeed > 0.6f ? 4 : 1) > 25
                    || data.playerInfo.deltaXZ - baseSpeed > 0.6f) {
                vl+= data.playerInfo.deltaXZ - baseSpeed > 0.6f ? 50 : 1;
                flag(data.playerInfo.deltaXZ + ">-" + baseSpeed);
            }
        } else verbose.subtract();

        debug("deltaXZ=%1 base=%2 vb=%3", data.playerInfo.deltaXZ, baseSpeed, verbose);
    }
}
