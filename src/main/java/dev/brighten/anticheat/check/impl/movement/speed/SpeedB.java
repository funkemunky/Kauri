package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MovementUtils;

@CheckInfo(name = "Speed (Type B)", description = "Ensures acceleration is legit.")
public class SpeedB extends Check {

    private float motionX, motionZ;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() || data.playerInfo.generalCancel) return;

        runMotionPrediction();
    }

    private void runMotionPrediction() {
        float friction = MovementUtils.getFriction(data);
        //We check if it's greater than one since technically the friction is 0 when in air.
        //Air resistance is a static 0.91. So when on the ground, the player is affected by air resistance and
        //friction. I state the 0.91f as a value for easier reading.
        float airResistance = 0.91f;
        float toMultiply = (friction > 0 ? airResistance * friction : airResistance);

        //This is the base movement that vanilla sets for players.
        float movementFactor = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer());

        //We multiply beforehand since the reduction is motion is done after the position is set.
        //So since we are calculated the to instead of the from, the from has already been done.
        motionX = data.playerInfo.lDeltaX * toMultiply;
        motionZ = data.playerInfo.lDeltaZ * toMultiply;

       // float moveFlyingFriction = data.playerInfo.serverGround ?

        //Now we run the moveFlying, which is the only modification done before position method is run.
        //Note that the position method really only changes the motion when collisions are made, so a collision check.
        //Will have to be frun.
       // moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, );
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
            float f1 = MathHelper.sin(data.playerInfo.from.yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(data.playerInfo.from.yaw * (float) Math.PI / 180.0F);
            this.motionX += (double) (strafe * f2 - forward * f1);
            this.motionZ += (double) (forward * f2 + strafe * f1);
        }
    }
}
