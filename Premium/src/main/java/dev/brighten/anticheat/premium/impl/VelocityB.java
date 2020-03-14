package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumAnimation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.enchantments.Enchantment;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 70, developer = true)
@Cancellable
public class VelocityB extends Check {

    private double vX, vZ, vY, pvX, pvZ;
    private boolean useEntity, tookVelocity;
    private MaxDouble verbose = new MaxDouble(50);
    private double maxThreshold;
    private long lastVelocity;
    private static double[] moveValues = new double[] {-0.98, 0, 0.98};

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet, long timeStamp) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            tookVelocity = true;
            vX = packet.getX();
            vY = packet.getY();
            vZ = packet.getZ();
            lastVelocity = timeStamp;
        }
    }

    @Packet
    public void onUseEntity(WrappedInUseEntityPacket packet) {
        if(!useEntity && (data.predictionService.lastSprint || (
                        data.getPlayer().getItemInHand() != null
                        && data.getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)))
                && packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            useEntity = true;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(tookVelocity
                && timeStamp - lastVelocity > data.lagInfo.ping
                && Math.abs(data.playerInfo.deltaY - vY) < 0.00001) {
            vX = pvX;
            vZ = pvZ;
            tookVelocity = false;
        }

        if(pvX != 0 || pvZ != 0) {
            double vX = pvX;
            double vZ = pvZ;
            double vXZ = 0;
            boolean found = false;
            for (double forward : moveValues) {
                for(double strafe : moveValues) {
                    moveFlying(strafe, forward, 0.026);

                    vXZ = MathUtils.hypot(pvX, pvZ);

                    if(MathUtils.getDelta(vXZ, data.playerInfo.deltaXZ) < 1E-8) {
                        found = true;
                        break;
                    }
                    pvX = vX;
                    pvZ = vZ;
                }
            }

            if(!found) {
                moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, 0.026);
                vXZ = MathUtils.hypot(pvX, pvZ);
            }

            double ratio = data.playerInfo.deltaXZ / vXZ;

            debug("ratio=%v.3", ratio);
            pvX *= 0.91;
            pvZ *= 0.91;

            if(Math.abs(pvX) < 0.005) pvX = 0;
            if(Math.abs(pvZ) < 0.005) pvZ = 0;
            if(data.playerInfo.clientGround) pvX = pvZ = 0;
        }
    }

    private void moveFlying(double strafe, double forward, double friction) {
        double f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = Math.sqrt(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            double f1 = Math.sin(data.playerInfo.to.yaw * Math.PI / 180.0F);
            double f2 = Math.cos(data.playerInfo.to.yaw * Math.PI / 180.0F);
            pvX += (strafe * f2 - forward * f1);
            pvZ += (forward * f2 + strafe * f1);
        }
    }
}
