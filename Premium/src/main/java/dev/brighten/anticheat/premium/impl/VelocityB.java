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

    private double vX, vZ, vY, svX, svZ;
    private boolean useEntity, sprint, tookVelocity;
    private String lastKey;
    private MaxDouble verbose = new MaxDouble(50);
    private double maxThreshold;
    private long velocityTS;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            svX = packet.getX();
            svZ = packet.getZ();
            vY = packet.getY();
            vX = vZ = 0;
            tookVelocity = true;
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
        if(Math.abs(vX) < 0.005) vX = 0;
        if(Math.abs(vZ) < 0.005) vZ = 0;

        if(tookVelocity && MathUtils.getDelta(data.playerInfo.deltaY, vY) < 0.0001) {
            velocityTS = timeStamp;
            vX = svX;
            vZ = svZ;
            maxThreshold = 99;
            debug("set velocity");
            svX = svZ = vY = 0;
            tookVelocity = false;
        }
        if((vX != 0 || vZ != 0) && !tookVelocity) {
            if(sprint && useEntity) {
                vX*= 0.6;
                vZ*= 0.6;
            }

            if(!data.blockInfo.blocksNear
                    && !data.blockInfo.inWeb
                    && !data.playerInfo.onLadder
                    && timeStamp - data.creation > 3000L
                    && timeStamp - data.playerInfo.lastServerPos > 100L
                    && !data.blockInfo.inLiquid
                    && !data.playerInfo.serverPos
                    && !data.playerInfo.canFly
                    && !data.playerInfo.creative) {

                double f4 = 0.91;

                if (data.playerInfo.lClientGround) {
                    f4 *= data.blockInfo.currentFriction;
                }

                if(!lastKey.equals(data.predictionService.key)) maxThreshold = 70;

                if(data.lagInfo.lastPingDrop.hasNotPassed(20)) maxThreshold = 50;

                double f = 0.16277136 / (f4 * f4 * f4);
                double f5;

                if (data.playerInfo.lClientGround) {
                    f5 = data.predictionService.aiMoveSpeed * f;
                } else {
                    f5 = sprint ? 0.026 : 0.02;
                }

                double pct;

                float forward = data.predictionService.moveForward;
                float strafe = data.predictionService.moveStrafing;

                if(data.playerInfo.usingItem) {
                    forward*= 0.2;
                    strafe*= 0.2;
                }

                //debug("motion: " + strafe + ", " + forward);

                moveFlying(strafe, forward, f5);

                double vXZ = MathUtils.hypot(vX, vZ);

                double ratio = data.playerInfo.deltaXZ / vXZ;
                //double ratio = MathUtils.hypot(data.playerInfo.deltaX / vX, data.playerInfo.deltaZ / vZ);
                pct = ratio * 100;

                if (pct < maxThreshold && data.playerInfo.lastUseItem.hasPassed(4)) {
                    if(verbose.add() > 36) {
                        vl++;
                        flag("pct=%v% buffer=%v", MathUtils.round(pct, 3),
                                MathUtils.round(verbose.value(), 1));
                    }
                } else verbose.subtract(maxThreshold <= 80 ? 0.05 : (data.lagInfo.lagging ? 0.35f : 0.2f));

                debug("pct=" + MathUtils.round(pct, 2) + "/" + maxThreshold
                        + " key=" + data.predictionService.key + " ani="
                        + data.playerInfo.usingItem + " sprint=" + data.playerInfo.sprinting
                        + " ground=" + data.playerInfo.lClientGround + " vl=" + verbose.value()
                        + " lastUse=" + data.playerInfo.lastUseItem.getPassed());

                //debug("vX=" + vX + " vZ=" + vZ);
                //debug("dX=" + data.playerInfo.deltaX + " dZ=" + data.playerInfo.deltaZ + " item=" +);

                vX *= f4;
                vZ *= f4;

                if(timeStamp - velocityTS > 350L) {
                    vX = vZ = 0;
                }
            } else vX = vZ = 0;
        }
        lastKey = data.predictionService.key;
        useEntity = false;
        sprint = data.playerInfo.sprinting;
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
            vX += (strafe * f2 - forward * f1);
            vZ += (forward * f2 + strafe * f1);
        }
    }
}
