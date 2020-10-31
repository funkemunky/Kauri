package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Speed (C)", description = "Checks if a player moves past the absolute maximum speed they can possible do.",
        punishVL = 34, developer = true)
@Cancellable
public class SpeedC extends Check {

    private float verbose;
    private double maxMove;
    private static final double forwardFactor = Math.sqrt(0.98 * 0.98 + 0.98 * 0.98);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel) {
            if(data.playerInfo.generalCancel && verbose > 0) verbose--;
            return;
        }

        float ai = (float)data.predictionService.aiMoveSpeed;
        float deltaXZ = (float) data.playerInfo.deltaXZ;
        float drag = data.playerInfo.lClientGround ? 0.91f * data.blockInfo.fromFriction : 0.91f;

        maxMove = getMaxMovement(ai, drag) * 2.2f;

        if(data.playerInfo.lastVelocity.hasNotPassed(30)) {
            maxMove = Math.max(maxMove,
                    Math.hypot(data.playerInfo.velocityX, data.playerInfo.velocityZ) * 2.5f);
        }

        if(deltaXZ > maxMove) {
            verbose+= deltaXZ > maxMove * 1.5 ? 3 : 1;

            if(++verbose > 2) {
                vl++;
                flag("[%v.3]>-[%v.3]", deltaXZ, maxMove);
            }
        } else if(verbose > 0) verbose-= 0.1f;

        debug("deltaXZ=%v.2 threshold=%v.2", deltaXZ, maxMove);
    }

    public static float getMaxMovement(float aiMoveSpeed, float friction) {
        float deltaXZ = 0;
        float max = 0;
        for(int i = 0 ; i < 20 ; i++) {
            float movement = aiMoveSpeed
                    * (0.16277136F / (float)Math.pow(friction * 0.91f, 3));

            if(i % 4 == 0) movement+= 0.2f;

            float f = movement / (float)forwardFactor;
            float strafe = 0.98f * f, forward = 0.98f * f;

            deltaXZ+= Math.hypot(forward * -1, strafe);

            max = Math.max(deltaXZ, max);

            deltaXZ*= friction;
        }

        return max;
    }
}