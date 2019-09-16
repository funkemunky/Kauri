package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.")
public class VelocityB extends Check {

    private double vX, vZ;
    private long timeStamp, ticks, airTicks, groundTicks;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            timeStamp = System.currentTimeMillis();
            vX = packet.getX();
            vZ = packet.getZ();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.clientGround) {
            groundTicks++;
            airTicks = 0;
        } else {
            airTicks++;
            groundTicks = 0;
        }

        if(data.lagInfo.lastPingDrop.hasNotPassed(40)) {
            vX = vZ = 0;
            ticks = 0;
            return;
        }

        if((vX != 0 || vZ != 0)) {
            if(airTicks > 2) {

                float predicted = (float) MathUtils.hypot(vX, vZ);
                float pct = data.playerInfo.deltaXZ / predicted;

                if(pct < 99.999) {
                    //TODO flag
                }

                debug("pct=" + pct);

                if(ticks++ > 4) {
                    vX = vZ = 0;
                    ticks = 0;
                }
                vX*= 0.91;
                vX*= 0.91;
            } else {
                vX = vZ = 0;
                ticks = 0;
            }
        }
    }
}
