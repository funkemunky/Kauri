package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@Cancellable
@CheckInfo(name = "Fly (F)", description = "Checks if an individual flys faster than possible.", executable = true,
        punishVL = 5,
        checkType = CheckType.FLIGHT)
public class FlyF extends Check {

    private double slimeY = 0;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(data.playerInfo.deltaXZ == 0 && data.playerInfo.deltaY == 0) return;

        double max = Math.max((data.playerInfo.clientGround && data.playerInfo.serverGround)
                ? 0.6001 : 0.5001, (data.playerInfo.lastVelocity.isNotPassed(20)
                ? Math.max(data.playerInfo.velocityY, data.playerInfo.jumpHeight)
                : data.playerInfo.jumpHeight) + 0.001);

        if(data.playerInfo.lastHalfBlock.isNotPassed(20)
                || data.blockInfo.collidesHorizontally) max = Math.max(0.5625, max);

        if(data.playerInfo.wasOnSlime && data.playerInfo.clientGround && data.playerInfo.nearGround) {
            slimeY = Math.abs(data.playerInfo.deltaY);
            max = Math.max(max, slimeY);
            debug("SLIME: sy=%.2f", slimeY);
        } else if(data.playerInfo.wasOnSlime && data.playerInfo.airTicks > 2) {
            slimeY-= 0.08f;
            slimeY*= 0.98f;

            debug("SLIME ACCEL: sy=%.2f", slimeY);
            max = Math.max(max, slimeY);
        } else if(!data.playerInfo.wasOnSlime && slimeY != 0) {
            slimeY = 0;
        }

        if(data.playerInfo.deltaY > max
                && !data.blockInfo.roseBush
                && data.playerInfo.lastVelocity.isPassed(2)
                && !data.playerInfo.doingVelocity
                && data.playerInfo.slimeTimer.isPassed(10)
                && !data.playerInfo.generalCancel) {
            ++vl;
            flag("dY=%.3f max=%.3f", data.playerInfo.deltaY, max);
        }

        debug("halfBlock=%s ticks=%s c/s=%s,%s", data.playerInfo.lastHalfBlock.getPassed(),
                data.blockInfo.onHalfBlock, data.playerInfo.clientGround, data.playerInfo.serverGround);
    }

}
