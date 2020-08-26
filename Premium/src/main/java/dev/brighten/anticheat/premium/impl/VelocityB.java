package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.enchantments.Enchantment;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 80, vlToFlag = 15)
@Cancellable
public class VelocityB extends Check {

    private double vX, vZ, vY, pvX, pvZ;
    private boolean useEntity, tookVelocity, sprint;
    private double buffer;
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
            pvX = pvZ = 0;
        }
    }

    @Packet
    public void onUseEntity(WrappedInUseEntityPacket packet) {
        if(!useEntity
                && packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            useEntity = true;
        }
    }

    @Packet
    public void onKeepAlive(WrappedInKeepAlivePacket packet) {
        if(tookVelocity && packet.getTime() == data.getKeepAliveStamp("velocity")) {
            tookVelocity = false;
            pvX = vX;
            pvZ = vZ;
            vY = -1;
            vX = 0;
            vZ = 0;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if((pvX != 0 || pvZ != 0)) {
            boolean found = false;

            double drag = 0.91;

            if(data.blockInfo.blocksNear
                    || data.blockInfo.inLiquid
                    || tookVelocity
                    || data.lagInfo.lastPingDrop.hasNotPassed(10)
                    || data.lagInfo.lastPacketDrop.hasNotPassed(10)) {
                pvX = pvZ = 0;
                buffer-= buffer > 0 ? 1 : 0;
                return;
            }

            if(data.playerInfo.lClientGround) {
                drag*= data.blockInfo.currentFriction;
            }

            if(useEntity && (sprint || (data.getPlayer().getItemInHand() != null
                    && data.getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)))) {
                pvX*= 0.6;
                pvZ*= 0.6;
            }
            double f = 0.16277136 / (drag * drag * drag);
            double f5;

            if (data.playerInfo.lClientGround) {
                f5 = data.predictionService.aiMoveSpeed * f;
            } else {
                f5 = sprint ? 0.026 : 0.02;
            }

            double vX = pvX;
            double vZ = pvZ;
            double vXZ = 0;

            double moveStrafe = 0, moveForward = 0;
            for (double forward : moveValues) {
                for(double strafe : moveValues) {
                    double s2 = strafe;
                    double f2 = forward;
                    if(data.playerInfo.usingItem) {
                        s2*= 0.2;
                        f2*= 0.2;
                    }
                    moveFlying(s2, f2, f5);

                    double deltaX = Math.abs(pvX - data.playerInfo.deltaX);
                    double deltaZ = Math.abs(pvZ - data.playerInfo.deltaZ);

                    pvX = vX;
                    pvZ = vZ;
                    if(deltaX <= 0.005 - (data.playerInfo.usingItem ? 0.0045 : 0)
                            && deltaZ <= 0.005 - (data.playerInfo.usingItem ? 0.0045 : 0)) {
                        moveForward = f2;
                        moveStrafe = s2;
                        found = true;
                        break;
                    }

                }
            }

            if(!found) {
                moveStrafe = data.predictionService.moveStrafing;
                moveForward = data.predictionService.moveForward;
                if(data.playerInfo.usingItem) {
                    moveStrafe*= 0.2;
                    moveForward*= 0.2;
                }
            }

            moveFlying(moveStrafe, moveForward, f5);

            vXZ = MathUtils.hypot(pvX, pvZ);

            double ratio = data.playerInfo.deltaXZ / vXZ;

            if(ratio < (data.playerVersion.isOrAbove(ProtocolVersion.V1_9) ? 0.8 : 0.993)
                    && timeStamp - data.creation > 3000L
                    && !data.getPlayer().getItemInHand().getType().isEdible()
                    && !data.blockInfo.blocksNear) {
                if(++buffer > 30) {
                    vl++;
                    flag("pct=%v.2% buffer=%v.1 forward=%v.2 strafe=%v.2",
                            ratio * 100, buffer, moveStrafe, moveForward);
                }
            } else buffer-= buffer > 0 ? data.lagInfo.lastPacketDrop.hasNotPassed(20) ? .5 : 0.25 : 0;
            debug("ratio=%v.3 buffer=%v.1 strafe=%v.2 forward=%v.2 lastUse=%v found=%v",
                    ratio, buffer, moveStrafe, moveForward, data.playerInfo.lastUseItem.getPassed(), found);
            pvX *= drag;
            pvZ *= drag;

            if(timeStamp - lastVelocity > 350L) pvX = pvZ = 0;

            if(Math.abs(pvX) < 0.005) pvX = 0;
            if(Math.abs(pvZ) < 0.005) pvZ = 0;
        }
        sprint = data.playerInfo.sprinting;
        useEntity = false;
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
