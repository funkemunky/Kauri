package dev.brighten.anticheat.check.impl.movement.velocity;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Velocity (B)", description = "A horizontally velocity check.")
public class VelocityB extends Check {

    private double vX, vZ;
    private long timeStamp, ticks, airTicks, groundTicks;
    private boolean didUseEntity;
    private float lastMS, lastMF, moveStrafing, moveForward;

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        if(packet.getId() == data.getPlayer().getEntityId()) {
            timeStamp = System.currentTimeMillis();
            vX = packet.getX();
            vZ = packet.getZ();
        }
    }

    @Packet
    public void onUseEntity(WrappedInUseEntityPacket packet) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            vX*= 0.6f;
            vZ*= 0.6f;
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        if(data.lagInfo.lastPingDrop.hasNotPassed(40)) {
            vX = vZ = 0;
            ticks = 0;
            return;
        }

        if((vX != 0 || vZ != 0)) {
            if(data.playerInfo.airTicks > 2 && !data.blockInfo.blocksNear) {
                if(MathUtils.millisToTicks(System.currentTimeMillis() - timeStamp) > MathUtils.millisToTicks(data.lagInfo.averagePing)) {
                    double lVX = vX, lVZ = vZ;

                    moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, data.playerInfo.sprinting ? 0.026f : 0.02f);

                    double lastPct = data.playerInfo.deltaXZ / MathUtils.hypot(vX, vZ) * 100;

                    vX = lVX;
                    vZ = lVZ;

                    moveFlying(lastMS, lastMF, data.playerInfo.sprinting ? 0.026f : 0.02f);

                    if(lastPct > data.playerInfo.deltaXZ / MathUtils.hypot(vX, vZ) * 100) {
                        moveForward = lastMF;
                        moveStrafing = lastMS;
                    } else {
                        vX = lVX;
                        vZ = lVZ;

                        moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, data.playerInfo.sprinting ? 0.026f : 0.02f);
                    }

                    float predicted = (float) MathUtils.hypot(vX, vZ);
                    float pct = data.playerInfo.deltaXZ / predicted * 100;

                    if (pct < 99.1) {
                        if(vl++ > 40) {
                            punish();
                        } else if(vl > 20) flag("pct=" + MathUtils.round(pct, 3) + "%");
                    } else vl-= vl > 0 ? 0.25 : 0;

                    debug("pct=" + pct + " key=" + data.predictionService.key);

                    if (ticks++ > 4) {
                        vX = vZ = 0;
                        ticks = 0;
                    }
                    vX *= 0.91;
                    vZ *= 0.91;
                }
            } else {
                vX = vZ = 0;
                ticks = 0;
            }
        }
        lastMF = data.predictionService.moveForward;
        lastMS = data.predictionService.moveStrafing;
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
