package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 28)
public class VelocityB extends Check {

    private double vX, vZ;
    private long velocityTS;
    private float forward, strafe;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet, long timeStamp) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            vX = packet.getX();
            vZ = packet.getZ();
            velocityTS = timeStamp;
        }
    }

    @Packet
    public void onUseEntity(WrappedInUseEntityPacket packet) {
        if( data.playerInfo.lastVelocity.hasNotPassed(5)
                && (data.predictionService.lastSprint || (
                        data.getPlayer().getItemInHand() != null
                        && data.getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK)))
                && packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            vX*= 0.6f;
            vZ*= 0.6f;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if((vX != 0 || vZ != 0)) {
            if(timeStamp - data.playerInfo.lastVelocityTimestamp < 400) {
                if(!data.blockInfo.blocksNear
                        && !data.blockInfo.inWeb
                        && !data.playerInfo.onLadder
                        && !data.blockInfo.inLiquid
                        && !data.lagInfo.lagging
                        && !data.playerInfo.serverPos
                        && !data.getPlayer().getAllowFlight()) {

                    float f4 = 0.91f;

                    if (data.playerInfo.lClientGround) {
                        f4 *= MovementUtils.getFriction(data);
                    }

                    float f = 0.16277136F / (f4 * f4 * f4);
                    float f5;

                    if (data.playerInfo.lClientGround) {
                        f5 = data.predictionService.aiMoveSpeed * f;
                    } else {
                        f5 = data.playerInfo.sprinting ? 0.026f : 0.02f;
                    }

                    double pct = 100;

                    forward = data.predictionService.moveForward;
                    strafe = data.predictionService.moveStrafing;

                    if(data.playerInfo.isAnimated) {
                        forward*= 0.2f;
                        strafe*= 0.2f;
                    }

                    debug("motion: " + strafe + ", " + forward);

                    moveFlying(strafe, forward, f5);

                    double vXZ = MathUtils.hypot(vX, vZ);
                    pct = data.playerInfo.deltaXZ / vXZ * 100;

                    if (pct < 99.4 && !data.playerInfo.usingItem && !data.predictionService.useSword) {
                        if (vl++ > 15) flag("pct=" + MathUtils.round(pct, 3) + "%");
                    } else vl -= vl > 0 ? 0.5 : 0;

                    debug("pct=" + pct + " key=" + data.predictionService.key + " ani="
                            + data.playerInfo.isAnimated + " sprint=" + data.playerInfo.sprinting
                            + " ground=" + packet.isGround() + " vl=" + vl);

                    //debug("vX=" + vX + " vZ=" + vZ);
                    //debug("dX=" + data.playerInfo.deltaX + " dZ=" + data.playerInfo.deltaZ + " item=" +);

                    vX *= f4;
                    vZ *= f4;

                    if(timeStamp - velocityTS > 300) {
                        vX = vZ = 0;
                    }
                } else vX = vZ = 0;
            }
        }
    }

    private void moveFlying(float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

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
