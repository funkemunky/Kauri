package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (C)", description = "Checks for vertical velocity mods on first tick only.",
        checkType = CheckType.VELOCITY, punishVL = 7)
public class VelocityC extends Check {

    private float velocityY;
    private long lastVelocity;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet, long timeStamp) {
        if(data.getPlayer().getEntityId() == packet.getId()
                && data.playerInfo.clientGround
                && packet.getY() > 0) {
            velocityY = (float) packet.getY();
        }
    }

    @Packet
    public void onKeepAlive(WrappedInKeepAlivePacket packet, long timeStamp) {
        if(packet.getTime() == 101L) {
            lastVelocity = timeStamp;
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
            float ratio = data.playerInfo.deltaY / velocityY,
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