package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (D)", description = "Ensures a user doesn't fly faster than the maximum threshold.",
        checkType = CheckType.FLIGHT, punishVL = 10, vlToFlag = 1, developer = true)
@Cancellable
public class FlyD extends Check {

    private double velocityY;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timestamp) {
        if(timestamp - data.playerInfo.lastVelocityTimestamp < 50L)
            velocityY = data.playerInfo.velocityY;

        if(packet.isPos()) {
            double threshold = MiscUtils.max(data.playerInfo.jumpHeight * 1.25, velocityY * 1.25);

            if(data.playerInfo.deltaY > threshold && !data.playerInfo.flightCancel) {
                vl++;
                flag("%v.2>-%v", data.playerInfo.deltaY, threshold);
            }

            debug("deltaY=%v.4 threshold=%v.4 velocity=%v.2", data.playerInfo.deltaY, threshold, velocityY);
            if(velocityY > 0) {
                velocityY-= 0.08;
                velocityY *= 0.98;
            } else if(velocityY < 0 || Math.abs(velocityY) < 0.005) {
                velocityY = 0;
            }
        }
    }
}
