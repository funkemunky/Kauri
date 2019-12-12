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
        if(packet.isPos() && (data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY != 0)) {

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
                f5 = data.predictionService.sprint ? .026f : .02f;
            }

            moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, f5);

            if(data.playerInfo.lDeltaY <= 0
                    && data.playerInfo.deltaY > 0
                    && (data.playerInfo.blocksAboveTicks > 0
                    || MathUtils.getDelta(data.playerInfo.deltaY, data.playerInfo.jumpHeight) < 1E-4)
                    && !data.playerInfo.clientGround
                    && data.playerInfo.lClientGround
                    && data.playerInfo.sprinting) {
                jump();
            }

            double pDeltaXZ = MathUtils.hypot(deltaX, deltaZ)
                    + (data.playerInfo.deltaYaw > 8 ? 0.007 : 0.002)
                    + (data.lagInfo.lastPacketDrop.hasNotPassed(5) ? 0.01f : 0);

            if(data.playerInfo.deltaXZ > pDeltaXZ
                    && !data.playerInfo.generalCancel
                    && data.playerInfo.lastVelocity.hasNotPassed(10)
                    && pDeltaXZ > 0.1f) {
                if((vl+= MathUtils.getDelta(data.playerInfo.deltaXZ, pDeltaXZ) > 0.2f ? 11 : 1) > 10) {
                    flag("your mom a hoe bitch");
                }
                debug(Color.Green + "Flag: " + vl);
            } else vl-= vl > 0 ? 0.5f : 0;

            debug("pDeltaXZ=" + MathUtils.trim(4, pDeltaXZ)
                    + " deltaXZ=" + MathUtils.trim(4, data.playerInfo.deltaXZ)
                    + " ground=" + data.playerInfo.clientGround
                    + " forward=" + (data.predictionService.moveForward != 0)
                    + " strafe=" + (data.predictionService.moveStrafing != 0));

            deltaX*= f4;
            deltaZ*= f4;

            if(data.playerInfo.lastVelocity.hasNotPassed(5)) {
                deltaX = data.playerInfo.deltaX;
                deltaZ = data.playerInfo.deltaZ;
            }
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
