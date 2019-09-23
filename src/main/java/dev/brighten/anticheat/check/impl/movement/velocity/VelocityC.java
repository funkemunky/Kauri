package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@Init
@CheckInfo(name = "Velocity (C)")
public class VelocityC extends Check {

    private double vX, vZ;

    @Packet
    public void velocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            vX = packet.getX();
            vZ = packet.getZ();
        }
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.lastVelocity.hasNotPassed(1) && data.playerInfo.airTicks > 1 && !data.playerInfo.clientGround) {
            double dX = data.playerInfo.lDeltaX - data.playerInfo.deltaX,
                    dZ = data.playerInfo.lDeltaZ - data.playerInfo.deltaZ;

            double dXZ = MathUtils.hypot(dX, dZ), vXZ = MathUtils.hypot(vX, vZ);

            debug("dXZ=" + dXZ + " vXZ=" + vXZ);
        }
    }
}
