package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Velocity (C)")
public class VelocityC extends Check {

    private double velocityX, velocityZ, velocityY;
    private TickTimer lastVelocity = new TickTimer(20);

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            velocityX = packet.getX();
            velocityY = packet.getY();
            velocityZ = packet.getZ();
            lastVelocity.reset();
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(velocityY > 0 && lastVelocity.hasPassed(MathUtils.millisToTicks(data.lagInfo.averagePing) - 1)) {
            debug("1: x=" + velocityX + " z=" + velocityZ + " y=" + velocityY);
            debug("2:" + "x=" + data.playerInfo.deltaX + " z=" + data.playerInfo.deltaZ + " y=" + data.playerInfo.deltaY);
            velocityY = velocityX = velocityZ = 0;
        }
    }

}