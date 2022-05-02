package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (C)", description = "General non-vanilla speed distance check",
        punishVL = 7, executable = true)
@Cancellable
public class SpeedC extends Check {

    private MaxInteger verbose = new MaxInteger(40);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos()
                || data.playerInfo.generalCancel
                || data.playerInfo.lastVelocity.isNotPassed(25)) {
            if(data.playerInfo.generalCancel)verbose.subtract();
            return;
        }

        double baseSpeed = data.playerInfo.baseSpeed + (!data.playerInfo.clientGround ? 0.06f
                : (data.playerInfo.groundTicks > 10 ? 0.02 : 0.03));

        baseSpeed+= data.playerInfo.iceTimer.isNotPassed(70) ? 0.4
                + (Math.min(60, 60 - data.playerInfo.iceTimer.getPassed()) * 0.015) : 0;
        baseSpeed+= data.playerInfo.blockAboveTimer.isNotPassed(20) ? 0.35
                + ((20 - data.playerInfo.blockAboveTimer.getPassed()) * 0.005) : 0;
        baseSpeed+= data.playerInfo.lastHalfBlock.isNotPassed(20)
                ? 0.2 + (20 - data.playerInfo.lastHalfBlock.getPassed()) * 0.005
                : 0;
        baseSpeed+= data.playerInfo.wasOnSlime ? 0.1 : 0;

        if(data.playerInfo.lastBlockPlace.isNotPassed(10))
            baseSpeed+= 0.2;

        if(data.playerInfo.baseSpeed < 0.2) return;

        if(data.playerInfo.deltaXZ > baseSpeed) {
            if(verbose.add(data.playerInfo.deltaXZ - baseSpeed > 0.45f ? 4 : 1) > 25
                    || data.playerInfo.deltaXZ - baseSpeed > 0.45f) {
                vl++;
                flag("%s>-%s",
                        MathUtils.round(data.playerInfo.deltaXZ, 3),
                        MathUtils.round(baseSpeed, 3));
            }
        } else verbose.subtract();

        debug("deltaXZ=%s base=%s vb=%s", data.playerInfo.deltaXZ, baseSpeed, verbose);
    }

}
