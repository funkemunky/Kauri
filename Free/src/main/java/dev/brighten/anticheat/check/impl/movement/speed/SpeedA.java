package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathHelper;
import cc.funkemunky.api.utils.PlayerUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (A)", description = "A simple limiting speed check with a high verbose threshold.",
        punishVL = 34, vlToFlag = 2)
@Cancellable
public class SpeedA extends Check {

    private boolean sprinting;
    private double deltaX, deltaZ, velocity;

    private static double keyVal = MathHelper.sqrt_double((0.98 * 0.98) + (0.98 * 0.98));

    @Packet
    public void onVelocity(WrappedOutVelocityPacket packet) {
        data.runKeepaliveAction(ka -> {
            velocity = Math.sqrt(Math.pow(Math.hypot(packet.getX(), packet.getZ()), 2) / 2);
            debug("velocity: %v.4", velocity);
        });
    }

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        check: {
            if(!packet.isPos()
                    || (data.playerInfo.deltaY == 0 && data.playerInfo.deltaXZ == 0)) {
                break check;
            }

            float aiMoveSpeed = data.getPlayer().getWalkSpeed() / 2f;

            if (data.playerInfo.sprinting) {
                aiMoveSpeed+= aiMoveSpeed * 0.30000001192092896D;
            }

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED)) {
                aiMoveSpeed += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED) * (0.20000000298023224D)) * aiMoveSpeed;
            }

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SLOW)) {
                aiMoveSpeed += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SLOW) * (-0.15000000596046448D)) * aiMoveSpeed;
            }

            double moveStrafing = 0.98, moveForward = 0.98, moveFactor;
            float friction = 0.91f;

            //We use the previous ground since this is what is used to calculate motion.
            if(data.playerInfo.lClientGround) {
                friction*= data.blockInfo.fromFriction;
                moveFactor = aiMoveSpeed * (0.16277136F / (friction * friction * friction));

                //Checking if a player is jumping.
                if(!data.playerInfo.clientGround
                        && data.playerInfo.deltaY > 0
                        && data.playerInfo.deltaY <= data.playerInfo.jumpHeight)
                    moveFactor+= 0.2;
            } else { //The values for the player in air.
                moveFactor = sprinting ? 0.026 : 0.02;
            }

            //Calculating the motion needed to be added.
            double keymotion = moveFactor / keyVal;
            moveStrafing*= keymotion;
            moveForward*= keymotion;

            deltaX+= moveStrafing;
            deltaZ+= moveForward;

            double threshold = Math.hypot(deltaX, deltaZ) * 1.15;

            if(!data.playerInfo.generalCancel && data.playerInfo.lastVelocity.hasPassed(15)) {
                if(data.playerInfo.deltaXZ > threshold) {
                    vl++;
                    flag("%v.3>%v.3", data.playerInfo.deltaXZ, threshold);
                } else if(vl > 0) vl-= 0.05;
                debug("deltaXZ=%v.5 threshold=%v.5 aimove=%v",
                        data.playerInfo.deltaXZ, threshold, aiMoveSpeed);
            }

            //Adding the friction deceleration for next calculation.
            deltaX*= friction;
            deltaZ*= friction;
        }
        sprinting = data.playerInfo.sprinting; //Setting the sprint for the next packet.
    }
}
