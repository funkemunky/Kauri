package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.MathHelper;
import dev.brighten.anticheat.data.ObjectData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VelocityProcessor {

    private final ObjectData data;
    public double x, y, z;
    private double mx, my, mz;

    void onFlying(WrappedInFlyingPacket packet) {
        /* Horizontal prediction */
        float friction = 0.91f * (data.predictionService.lastOnGround ? data.blockInfo.currentFriction : 1);

        float frictionFactor = 0.16277136F / (friction * friction * friction);
        float drag;
        if (data.playerInfo.lClientGround) {
            drag = (float)data.predictionService.aiMoveSpeed * frictionFactor;
        } else {
            drag = data.playerInfo.sprinting ? 0.026f : 0.02f;
        }

        double motionX = mx;
        double motionZ = mz;

        float moveStrafing = data.predictionService.moveStrafing, moveForward = data.predictionService.moveForward;

        float var14 = moveStrafing * moveStrafing + moveForward * moveForward;
        if (var14 >= 1.0E-4F) {
            var14 = MathHelper.sqrt_float(var14);
            if (var14 < 1.0F)
                var14 = 1.0F;
            var14 = drag / var14;
            moveStrafing *= var14;
            moveForward *= var14;

            final float var15 = MathHelper.sin(data.playerInfo.from.yaw * (float) Math.PI / 180.0F);
            final float var16 = MathHelper.cos(data.playerInfo.from.yaw * (float) Math.PI / 180.0F);
            motionX += (moveStrafing * var16 - moveForward * var15);
            motionZ += (moveForward * var16 + moveStrafing * var15);
        }

        x = mx = motionX;
        x = mz = motionZ;

        mx*= friction;
        mz*= friction;
    }

    void onTransaction(WrappedInTransactionPacket packet) {
        if(packet.getAction() == (short) 101) {
            x = data.playerInfo.velocityX;
            y = data.playerInfo.velocityY;
            z = data.playerInfo.velocityZ;
        }
    }
}
