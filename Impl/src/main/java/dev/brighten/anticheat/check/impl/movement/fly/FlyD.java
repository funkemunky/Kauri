package dev.brighten.anticheat.check.impl.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Fly (D)", description = "Ensures a user doesn't fly faster than the maximum threshold.",
        checkType = CheckType.FLIGHT, punishVL = 10, vlToFlag = 1, developer = true)
@Cancellable
public class FlyD extends Check {

    private double velocityY;

    @Packet
    public void onKeepAlive(WrappedInKeepAlivePacket packet) {
        if(packet.getTime() == data.getKeepAliveStamp("velocity")) {
            velocityY = data.playerInfo.velocityY;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            double threshold = MiscUtils.max(0.8, data.playerInfo.jumpHeight * 2, velocityY * 1.25);

            if(data.playerInfo.deltaY > threshold) {
                vl++;

            }
        }
        if(data.playerInfo.lastVelocity.hasPassed(20)) {
            velocityY = 0;
        }
    }
}
