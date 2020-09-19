package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (C)", description = "A simple limiting speed check with a high verbose threshold.",
        punishVL = 34, vlToFlag = 2)
@Cancellable
public class SpeedC extends Check {

    private int verbose;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel || data.playerInfo.lastVelocity.hasNotPassed(25)) {
            if(data.playerInfo.generalCancel && verbose > 0) verbose--;
            return;
        }

        double baseSpeed = data.playerInfo.baseSpeed + (!data.playerInfo.clientGround ? 0.06f
                : (data.playerInfo.groundTicks > 10 ? 0.02 : 0.03));

        baseSpeed+= data.playerInfo.iceTimer.hasNotPassed(70) ? 0.4
                + (Math.min(60, 60 - data.playerInfo.iceTimer.getPassed()) * 0.015) : 0;
        baseSpeed+= data.playerInfo.blockAboveTimer.hasNotPassed(20) ? 0.35
                + ((20 - data.playerInfo.blockAboveTimer.getPassed()) * 0.005) : 0;
        baseSpeed+= data.playerInfo.lastHalfBlock.hasNotPassed(20)
                ? 0.2 + (20 - data.playerInfo.lastHalfBlock.getPassed()) * 0.005
                : 0;
        baseSpeed+= data.playerInfo.wasOnSlime ? 0.1 : 0;

        if(data.playerInfo.lastBlockPlace.hasNotPassed(10))
            baseSpeed+= 0.2;

        if(data.playerInfo.deltaXZ > baseSpeed) {
            if(++verbose > 25
                    || data.playerInfo.deltaXZ - baseSpeed > 0.45f) {
                vl++;
                flag("%v>-%v",
                        MathUtils.round(data.playerInfo.deltaXZ, 3),
                        MathUtils.round(baseSpeed, 3));
            }
        } else if(verbose > 0) verbose--;

        debug("deltaXZ=%v base=%v vb=%v", data.playerInfo.deltaXZ, baseSpeed, verbose);
    }
}