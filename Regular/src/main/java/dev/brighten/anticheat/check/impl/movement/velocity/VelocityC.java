package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Velocity (C)", description = "A simple horizontal velocity check.", checkType = CheckType.VELOCITY,
        developer = true)
public class VelocityC extends Check {

    private double vx, vz;
    private int ticks, buffer;
    private boolean attacked;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() != data.getPlayer().getEntityId()) return;

        vx = vz = 0;
        data.runKeepaliveAction(ka -> {
            vx = packet.getX();
            vz = packet.getZ();
        });
    }

    @Packet
    public void onAttack(WrappedInUseEntityPacket packet) {
        attacked = true;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(vx != 0 || vz != 0) {
            double drag = 0.91;

            if(data.blockInfo.blocksNear) {
                vx = vz = 0;
                return;
            }

            if(data.playerInfo.lClientGround) {
                drag*= data.blockInfo.fromFriction;
            }

            if(attacked) {
                vx*= 0.6;
                vz*= 0.6;
            }

            double deltaX = data.playerInfo.deltaX, deltaZ = data.playerInfo.deltaZ;
            double weightX = deltaX / vx, weightZ = deltaZ / vz;
            double weight = Math.hypot(weightX, weightZ);

            if(weight < 0.5) {
                buffer+= 2;
                if(++buffer > 17) {
                    vl++;
                    flag("weight=%v.2 buffer=%v", weight, buffer);
                }
                debug(Color.Green + "Flag: " + buffer);
            } else if(buffer > 0) buffer--;

            debug("(%v) w=%v.2 dx=%v.4 dz=%v.4 vx=%v.4 vz=%v.4",
                    buffer, weight, deltaX, deltaZ, vx, vz);

            vx*= drag;
            vz*= drag;
            if(++ticks > 5) {
                vx = vz = 0;
                ticks = 0;
            }
        }
        attacked = false;
    }
}
