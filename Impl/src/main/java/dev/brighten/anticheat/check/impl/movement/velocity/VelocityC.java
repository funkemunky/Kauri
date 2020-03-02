package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (C)", description = "Checks for vertical velocity mods on first tick only.",
        checkType = CheckType.VELOCITY, punishVL = 7)
@Cancellable
public class VelocityC extends Check {

    private double velocityY;
    private long lastVelocity;

    @Packet
    public void onKeepAlive(WrappedInTransactionPacket packet, long timeStamp) {
        if(packet.getAction() == (short)101) {
            lastVelocity = timeStamp;
            velocityY = data.playerInfo.velocityY;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        long delta = timeStamp - lastVelocity;

        if(velocityY > 0
                && (!data.playerInfo.clientGround
                || (data.playerInfo.serverGround
                && data.lagInfo.lastPacketDrop.hasPassed(2)
                && timeStamp - lastVelocity < 52L))) {
            double ratio = (data.playerInfo.to.y - data.playerInfo.from.y) / velocityY,
                    pct = MathUtils.round(ratio * 100, 4);

            if(!data.blockInfo.blocksAbove) {
                if(ratio < 1) {
                    if(vl++ > 3) {
                        flag("pct=" + pct + "%");
                    }
                } else vl-= vl > 0 ? 0.25 : 0;
                debug("ratio=" + MathUtils.round(ratio, 4) + " vl=" + vl);
            }

            velocityY = 0;
        }
    }
}