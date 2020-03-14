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
import dev.brighten.anticheat.data.classes.PredictionService;
import dev.brighten.api.check.CheckType;
import org.bukkit.enchantments.Enchantment;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.", checkType = CheckType.VELOCITY,
        punishVL = 70, developer = true)
@Cancellable
public class VelocityB extends Check {

    private double vX, vZ, vY, pvX, pvZ;
    private boolean useEntity, tookVelocity, sprint;
    private long lastVelocity;
    private static float[] moveValues = new float[] {-0.98f, 0, 0.98f};
    private static boolean[] tandf = new boolean[] {true, false};

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
                && timeStamp - lastVelocity > data.lagInfo.ping / 1.1
                && Math.abs(data.playerInfo.deltaY - vY) < 0.001) {
            pvX = vX;
            pvZ = vZ;
            tookVelocity = false;
        }

        if(pvX != 0 || pvZ != 0) {
            if(useEntity) {
                pvX*= 0.6;
                pvZ*= 0.6;
                debug("useEntity");
            }
            double vX = pvX;
            double vZ = pvZ;
            double vXZ = 0;

            float drag = 0.91f;

            if (data.playerInfo.lClientGround) {
                drag *= data.blockInfo.currentFriction;
            }
            float f = 0.16277136f / (drag * drag * drag);
            float f5;

            if (data.playerInfo.lClientGround) {
                f5 = (float)data.predictionService.aiMoveSpeed * f;
            } else f5 = sprint ? 0.026f : 0.02f;

            boolean found = false;
            for(boolean fastMath : tandf) {
                data.predictionService.fastMath = fastMath;
                for (float forward : moveValues) {
                    for(float strafe : moveValues) {
                        moveFlying(strafe, forward, f5);

                        double deltaX = MathUtils.getDelta(pvX, data.playerInfo.deltaX);
                        double deltaZ = MathUtils.getDelta(pvZ, data.playerInfo.deltaZ);

                        if(deltaX < 0.01 && deltaZ <  0.01) {
                            found = true;
                            break;
                        } else {
                            pvX = vX;
                            pvZ = vZ;
                        }
                    }
                    if(found) break;
                }
            }

            if(!found) {
                moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, f5);
            }
            vXZ = MathUtils.hypot(pvX, pvZ);

            double ratio = data.playerInfo.deltaXZ / vXZ;

            if(ratio < 0.96) {
                flag("ratio=%v.3", ratio);
            }

            debug("ratio=%v.3 found=%v client=%v", ratio, found, data.playerInfo.lClientGround);
            pvX *= drag;
            pvZ *= drag;

            if(Math.abs(pvX) < 0.005) pvX = 0;
            if(Math.abs(pvZ) < 0.005) pvZ = 0;
            if(data.playerInfo.clientGround) pvX = pvZ = 0;
        }
        sprint = data.playerInfo.sprinting;
        useEntity = false;
    }

    private void moveFlying(float strafe, float forward, float var5) {
        float var14 = strafe * strafe + forward * forward;
        if (var14 >= 1.0E-4F) {
            var14 = PredictionService.sqrt_double(var14);
            if (var14 < 1.0F)
                var14 = 1.0F;
            var14 = var5 / var14;
            strafe *= var14;
            forward *= var14;

            final float var15 = data.predictionService.sin(data.playerInfo.to.yaw * (float) Math.PI / 180.0F); // cos, sin = Math function of optifine
            final float var16 = data.predictionService.cos(data.playerInfo.to.yaw * (float) Math.PI / 180.0F);
            pvX += (strafe * var16 - forward * var15);
            pvZ += (forward * var16 + strafe * var15);
        }
    }
}
