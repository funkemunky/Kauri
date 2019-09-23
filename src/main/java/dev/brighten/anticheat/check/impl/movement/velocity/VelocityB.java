package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.")
public class VelocityB extends Check {

    private double vX, vZ;
    private long  ticks;
    private String lastKey;
    private boolean attackedSinceVelocity;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            vX = packet.getX();
            vZ = packet.getZ();
        }
    }

    @Packet
    public void onUseEntity(WrappedInUseEntityPacket packet) {
        if((vX != 0 || vZ != 0)
                && !attackedSinceVelocity
                && packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            vX*= 0.6f;
            vZ*= 0.6f;
            attackedSinceVelocity = true;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        if(data.lagInfo.lastPingDrop.hasNotPassed(40)) {
            vX = vZ = 0;
            ticks = 0;
            return;
        }

        if(data.playerInfo.lastVelocity.hasNotPassed(1)) attackedSinceVelocity = false;

        if((vX != 0 || vZ != 0)) {
            if(!data.blockInfo.blocksNear
                    && data.playerInfo.airTicks > 1
                    && !data.playerInfo.lClientGround) {
                if(data.playerInfo.lastVelocity.hasNotPassed(3)) {
                    double lVX = vX, lVZ = vZ;

                    float f4 = 0.91f;

                    if(packet.isGround()) {
                        f4*= MovementUtils.getFriction(data);
                    }

                    float f = 0.16277136F / (f4 * f4 * f4);
                    float f5;

                    if (packet.isGround()) {
                        f5 = data.predictionService.aiMoveSpeed * f;
                    } else {
                        f5 = data.playerInfo.sprinting ? 0.026f : 0.02f;
                    }

                    moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, f5);

                    float predicted = (float) MathUtils.hypot(vX, vZ);
                    float pct = data.playerInfo.deltaXZ / predicted * 100;

                    float lPct = pct;
                    if(pct < 100) {
                        vX = lVX;
                        vZ = lVZ;

                        debug("lPCT=" + pct);
                        if(!lastKey.contains("S") && data.predictionService.key.contains("S")) {
                            moveFlying(0.98f,0, f5);
                            predicted = (float) MathUtils.hypot(vX, vZ);
                            pct = data.playerInfo.deltaXZ / predicted * 100;
                        } else if(!lastKey.contains("W") && data.predictionService.key.contains("W")) {
                            moveFlying(-0.98f,0, f5);
                            predicted = (float) MathUtils.hypot(vX, vZ);
                            pct = data.playerInfo.deltaXZ / predicted * 100;
                        }

                        if(MathUtils.getDelta(pct, 100) > MathUtils.getDelta(lPct, 100)) {
                            vX = lVX;
                            vZ = lVZ;

                            moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, f5);
                            predicted = (float) MathUtils.hypot(vX, vZ);
                            pct = data.playerInfo.deltaXZ / predicted * 100;
                        }
                    }

                    if (pct < (data.predictionService.key.equals("Nothing") ? 99 : 96.2)
                            && !data.predictionService.key.contains("D")
                            && !data.predictionService.key.contains("A")) {
                        if(vl++ > 22) {
                            punish();
                        } else if(vl > 12) flag("pct=" + MathUtils.round(pct, 3) + "%");
                    } else vl-= vl > 0 ? 0.25 : 0;

                    debug("pct=" + pct + " key=" + data.predictionService.key
                            + " sprint=" + data.playerInfo.sprinting + " ground=" + packet.isGround() + " vl=" + vl);

                    if (ticks++ > 5) {
                        vX = vZ = 0;
                        ticks = 0;
                    }

                    f4 = 0.91f;

                    if(data.playerInfo.clientGround ||  data.playerInfo.jumped) {
                        f4*= MovementUtils.getFriction(data);
                    }

                    vX *= f4;
                    vZ *= f4;
                }
            } else {
                vX = vZ = 0;
                ticks = 0;
            }
        }
        lastKey = data.predictionService.key;
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
