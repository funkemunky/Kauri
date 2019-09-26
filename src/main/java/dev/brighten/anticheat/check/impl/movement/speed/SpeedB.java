package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflection.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumAnimation;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (B)", description = "Ensures acceleration is legit.")
public class SpeedB extends Check {

    private float motionX, motionZ;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos() ||
                data.playerInfo.generalCancel
                || data.playerInfo.lastVelocity.hasPassed(5 + MathUtils.millisToTicks(data.lagInfo.ping))
                || data.playerInfo.liquidTicks > 0
                || data.playerInfo.webTicks > 0
                || Atlas.getInstance().getBlockBoxManager().getBlockBox().isUsingItem(data.getPlayer())
                || data.blockInfo.blocksAbove
                || data.blockInfo.blocksNear
                || data.playerInfo.halfBlockTicks > 0) {
            vl-= vl > 0 ? 0.25 : 0;
            return;
        }

        if (data.playerInfo.airTicks > 1) {
            runMotionPrediction();
            float predicted = (float) MathUtils.hypot(motionX, motionZ);

            if(MathUtils.getDelta(predicted, data.playerInfo.deltaXZ) > (data.playerInfo.deltaXZ > 0.23 ? 0.001 : 0.1)
                    && data.playerInfo.deltaYaw < 5) {
                if(vl++ > 50) {
                    punish();
                } else if(vl > 10) flag("predicted=" + predicted + " deltaXZ=" + data.playerInfo.deltaXZ);
            } else vl-= vl > 0 ? 2 : 0;

            debug("predicted=" + predicted + " deltaXZ=" + data.playerInfo.deltaXZ + " vl=" + vl);
        }
    }

    private void runMotionPrediction() {
        float friction = MovementUtils.getFriction(data);
        //We check if it's greater than one since technically the friction is 0 when in air.
        //Air resistance is a static 0.91. So when on the ground, the player is affected by air resistance and
        //friction. I state the 0.91f as a value for easier reading.
        float airResistance = 0.91f;
        float toMultiply = (data.playerInfo.clientGround ? airResistance * friction : airResistance);

        //This is the base movement that vanilla sets for players.
        float movementFactor = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer());

        //We multiply beforehand since the reduction is motion is done after the position is set.
        //So since we are calculated the to instead of the from, the from has already been done.
        motionX = data.playerInfo.lDeltaX * toMultiply;
        motionZ = data.playerInfo.lDeltaZ * toMultiply;

        float moveFlyingFriction = data.playerInfo.clientGround ? movementFactor
                * (0.16277136F / (toMultiply * toMultiply * toMultiply))
                : (data.playerInfo.sprinting ? 0.026f : 0.02f);

        //Now we run the moveFlying, which is the only modification done before position method is run.
        //Note that the position method really only changes the motion when collisions are made, so a collision check.
        //Will have to be frun.
        moveFlying(data.predictionService.moveStrafing, data.predictionService.moveForward, moveFlyingFriction);
    }

    private void jump() {
        if (data.playerInfo.sprinting) {
            float f = data.playerInfo.to.yaw * 0.017453292F;
            this.motionX -= (MathHelper.sin(f) * 0.2F);
            this.motionZ += (MathHelper.cos(f) * 0.2F);
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
            float f1 = MathHelper.sin(data.playerInfo.from.yaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(data.playerInfo.from.yaw * (float) Math.PI / 180.0F);
            this.motionX += (double) (strafe * f2 - forward * f1);
            this.motionZ += (double) (forward * f2 + strafe * f1);
        }
    }
}
