package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Speed (B)", description = "Predicts the motion of a player accurately.", developer = true,
        executable = false, punishVL = 150)
public class SpeedB extends Check {

    private float deltaX, deltaZ;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {

            if(Math.abs(deltaX) < 0.005) deltaX = 0;
            if(Math.abs(deltaZ) < 0.005) deltaZ = 0;

            float f4 = 0.91F;

            if (data.playerInfo.lClientGround) {
                f4 = MovementUtils.getFriction(data) * 0.91F;
            }

            float f = 0.16277136F / (f4 * f4 * f4);
            float f5;

            if (data.playerInfo.lClientGround) {
                f5 = data.predictionService.aiMoveSpeed
                        * f;
            } else {
                f5 = data.predictionService.sprint ? .026f : 0.02f;
            }

            moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, f5);

            if(data.playerInfo.lDeltaY <= 0
                    && data.playerInfo.deltaY > 0
                    && data.playerInfo.lClientGround && data.playerInfo.sprinting) {
                jump();
            }

            double pDeltaXZ = MathUtils.hypot(deltaX, deltaZ);

            if(data.playerInfo.deltaXZ > pDeltaXZ + 0.002f
                    && !data.playerInfo.generalCancel
                    && pDeltaXZ > 0 && pDeltaXZ > 0.2f) {
                if((vl+= data.playerInfo.deltaXZ - pDeltaXZ > 0.2f ? 4 : 1) > 3) {
                    flag("your mom a hoe bitch");
                }
            } else vl-= vl > 0 ? 0.25f : 0;

            debug("pDeltaXZ=" + pDeltaXZ + " deltaXZ=" + data.playerInfo.deltaXZ);

            deltaX*= f4;
            deltaZ*= f4;
        } else deltaX = deltaZ = 0;
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
            deltaX += (strafe * f2 - forward * f1);
            deltaZ += (forward * f2 + strafe * f1);
        }
    }

    private void jump() {
        float f = data.playerInfo.to.yaw * 0.017453292F;
        this.deltaX -= (double) (MathHelper.sin(f) * 0.2F);
        this.deltaZ += (double) (MathHelper.cos(f) * 0.2F);
    }
}
