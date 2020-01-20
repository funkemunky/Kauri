package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import dev.brighten.api.check.CheckType;
import org.bukkit.enchantments.Enchantment;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 36)
@Cancellable
public class VelocityB extends Check {

    private double vX, vZ, svX, svZ;
    private boolean useEntity, sprint;
    private float forward, strafe;
    private String lastKey;
    private long velocityTS;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            svX = packet.getX();
            svZ = packet.getZ();
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
    public void onTransaction(WrappedInTransactionPacket packet, long timeStamp) {
        if(packet.getAction() == (short)101) {
            velocityTS = timeStamp;
            vX = svX;
            vZ = svZ;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(!data.predictionService.key.equals(lastKey)) vX = vZ = 0;
        if((vX != 0 || vZ != 0)) {
            if(timeStamp - velocityTS < 400) {
                if(sprint && useEntity) {
                    vX*= 0.6;
                    vZ*= 0.6;
                }

                if(data.lagInfo.lastPacketDrop.hasNotPassed(1)
                        && data.lagInfo.lastPingDrop.hasNotPassed(100)) vX = vZ = 0;
                if(!data.blockInfo.blocksNear
                        && !data.blockInfo.inWeb
                        && data.predictionService.key.equals(lastKey)
                        && !data.playerInfo.onLadder
                        && !data.blockInfo.inLiquid
                        && !data.playerInfo.serverPos
                        && !data.getPlayer().getAllowFlight()) {

                    double f4 = 0.91;

                    if (data.playerInfo.lClientGround) {
                        f4 *= data.blockInfo.currentFriction;
                    }

                    double f = 0.16277136 / (f4 * f4 * f4);
                    double f5;

                    if (data.playerInfo.lClientGround) {
                        f5 = data.predictionService.aiMoveSpeed * f;
                    } else {
                        f5 = data.playerInfo.sprinting ? 0.026 : 0.02;
                    }

                    double pct;

                    forward = data.predictionService.moveForward;
                    strafe = data.predictionService.moveStrafing;

                    if(data.playerInfo.usingItem) {
                        forward*= 0.2;
                        strafe*= 0.2;
                    }

                    debug("motion: " + strafe + ", " + forward);

                    moveFlying(strafe, forward, f5);

                    double vXZ = MathUtils.hypot(vX, vZ);
                    pct = data.playerInfo.deltaXZ / vXZ * 100;

                    if (pct < 99.8
                            && !data.playerInfo.usingItem && !data.predictionService.useSword) {
                        if(data.lagInfo.lastPacketDrop.hasPassed(1)) {
                            if (strafe == 0 && forward == 0 && !data.lagInfo.lagging) vl+= 2;
                            if ((vl+= strafe == 0 && forward > 0 ? 1 : 0.5)
                                    > (data.lagInfo.transPing > 150 ? 22 : 15)) flag("pct=" + MathUtils.round(pct, 3) + "%");
                        }
                    } else vl -= vl > 0 ? data.lagInfo.lagging || data.lagInfo.transPing > 150 ? 0.5f : 0.2f : 0;

                    debug("pct=" + pct + " key=" + data.predictionService.key + " ani="
                            + data.playerInfo.usingItem + " sprint=" + data.playerInfo.sprinting
                            + " ground=" + data.playerInfo.lClientGround + ", " + data.playerInfo.clientGround
                            + " vl=" + vl);

                    //debug("vX=" + vX + " vZ=" + vZ);
                    //debug("dX=" + data.playerInfo.deltaX + " dZ=" + data.playerInfo.deltaZ + " item=" +);

                    vX *= f4;
                    vZ *= f4;

                    if(timeStamp - velocityTS > 170L) {
                        vX = vZ = 0;
                    }
                } else vX = vZ = 0;
            }
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
            float f1 = MathHelper.sin(data.playerInfo.to.yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(data.playerInfo.to.yaw * (float) Math.PI / 180.0F);
            vX += (strafe * f2 - forward * f1);
            vZ += (forward * f2 + strafe * f1);
        }
    }
}
