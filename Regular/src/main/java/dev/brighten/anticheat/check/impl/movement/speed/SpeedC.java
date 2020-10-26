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

    private int verbose;
    private float lfriction, maxMove, lai;
    private static double forwardFactor = Math.sqrt(0.98 * 0.98 + 0.98 * 0.98);

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel || data.playerInfo.lastVelocity.hasNotPassed(25)) {
            if(data.playerInfo.generalCancel && verbose > 0) verbose--;
            return;
        }

        float ai = (float)data.predictionService.aiMoveSpeed;
        float deltaXZ = (float) data.playerInfo.deltaXZ;
        float drag = data.playerInfo.lClientGround ? 0.91f * data.blockInfo.fromFriction : 0.91f;

        maxMove = getMaxMovement(data.playerInfo.clientGround, data.playerInfo.lClientGround ? ai : 0.026f, drag) * 2.5f;

        if(deltaXZ > maxMove) {
            flag("[%v.3]>-[%v.3]", deltaXZ, maxMove);
        }

        debug("deltaXZ=%v.2 threshold=%v.2", deltaXZ, maxMove);

        lfriction = data.blockInfo.fromFriction;
        lai = ai;
    }

    public static float getMaxMovement(boolean onGround, float aiMoveSpeed, float friction) {

        float deltaXZ = 0;
        float max = 0;
        for(int i = 0 ; i < 20 ; i++) {
            float movement = onGround ? aiMoveSpeed
                    * (0.16277136F / (float)Math.pow(friction, 3)) : 0.026f;

            if(i % 4 == 0) movement+= 0.2f;

            float f = movement / (float)forwardFactor;
            float strafe = 0.98f * f, forward = 0.98f * f;

            deltaXZ+= Math.hypot(0, strafe);

            max = Math.max(deltaXZ, max);

            deltaXZ*= friction;
        }

        return max;
    }
}