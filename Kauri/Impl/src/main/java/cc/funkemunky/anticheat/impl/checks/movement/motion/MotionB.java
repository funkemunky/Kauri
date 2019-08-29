package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.*;
import lombok.val;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@Init
@CheckInfo(name = "Motion (Type B)", description = "Checks to make sure horizontal movement is legitimate.", type = CheckType.MOTION, maxVL = 50, developer = true)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.FLYING, Packet.Client.LOOK, Packet.Server.ENTITY_VELOCITY})
public class MotionB extends Check {

    private float motionX, motionY, motionZ;
    private boolean jumped;
    private boolean isAirBorne;
    private Vector velocity;
    private long velocityTimestamp;
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if(packetType.equals(Packet.Server.ENTITY_VELOCITY)) {
            WrappedOutVelocityPacket velocity = new WrappedOutVelocityPacket(packet, getData().getPlayer());

            if(velocity.getId() == getData().getPlayer().getEntityId()) {
                this.velocity = new Vector(velocity.getX(), velocity.getY(), velocity.getZ());
                velocityTimestamp = timeStamp;
            }
        } else {
            val move = getData().getMovementProcessor();

            runMovement();

            if(!getData().isGeneralCancel() && !getData().getActionProcessor().isUsingItem()) {
                val predicted = new Vector(motionX, motionY, motionZ);

                val distance = predicted.distance(move.getTo().toVector());
            }

            moveEntityWithHeading(0,0, true);

            if(move.isServerPos()) motionX = motionY = motionZ = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }


    private void runMovement() {
        val move = getData().getMovementProcessor();

        if(velocity != null && (System.currentTimeMillis() - velocityTimestamp) > getData().getTransPing()) {
            motionX = (float) velocity.getX();
            motionY = (float) velocity.getY();
            motionZ = (float) velocity.getZ();
        }

        if(Math.abs(motionX) < 0.005) motionX = 0;
        if(Math.abs(motionY) < 0.005) motionY = 0;
        if(Math.abs(motionZ) < 0.005) motionZ = 0;

        float bmotionx = motionX, bmotiony = motionY, bmotionz = motionZ;

        moveEntityWithHeading(0, 0.98f, false);

        if(!isAirBorne && MathUtils.approxEquals(0.01, move.getDeltaY(), cc.funkemunky.anticheat.api.utils.MiscUtils.getPredictedJumpHeight(getData()))) {
            jump();
        }

        val distanceOne = new Vector(motionX, motionY, motionZ).distance(move.getTo().toVector());

        motionX = bmotionx;
        motionY = bmotiony;
        motionZ = bmotionz;

        moveEntityWithHeading(0.98f, 0.98f, false);

        if(!isAirBorne && MathUtils.approxEquals(0.01, move.getDeltaY(), cc.funkemunky.anticheat.api.utils.MiscUtils.getPredictedJumpHeight(getData()))) {
            jump();
        }

        val distanceTwo = new Vector(motionX, motionY, motionZ).distance(move.getTo().toVector());

        motionX = bmotionx;
        motionY = bmotiony;
        motionZ = bmotionz;

        if(distanceOne < distanceTwo) {
            moveEntityWithHeading(0, 0.98f, false);

            if(!isAirBorne && MathUtils.approxEquals(0.01, move.getDeltaY(), cc.funkemunky.anticheat.api.utils.MiscUtils.getPredictedJumpHeight(getData()))) {
                jump();
            }
        } else {
            moveEntityWithHeading(0.98f, 0.98f, false);

            if(!isAirBorne && MathUtils.approxEquals(0.01, move.getDeltaY(), cc.funkemunky.anticheat.api.utils.MiscUtils.getPredictedJumpHeight(getData()))) {
                jump();
            }
        }
    }
    private void moveEntityWithHeading(float strafe, float forward, boolean after) {
        val move = getData().getMovementProcessor();
        float f4 = 0.91F;

        if (move.isClientOnGround()) {
            isAirBorne = false;
            f4 = (getData().getBlockBelow() != null ? ReflectionsUtil.getFriction(getData().getBlockBelow()) : 0.68f) * 0.91F;
        }

        if(!after) {
            float f = 0.16277136F / (f4 * f4 * f4);
            float f5;

            if (move.isClientOnGround()) {
                f5 = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(getData().getPlayer()) * f;
            } else {
                f5 = getData().getActionProcessor().isSprinting() ? 0.026f : 0.02f;
            }

            this.moveFlying(strafe, forward, f5);
        } else {
            motionX*= f4;
            motionY -= 0.08D;
            motionY*= 0.9800000190734863D;
            motionZ*= f4;

            if(motionY < 0 && move.isClientOnGround()) {
                motionY = 0;
            }
        }
    }

    private void moveFlying(float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;
        val move = getData().getMovementProcessor();

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(move.getTo().getYaw() * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(move.getTo().getYaw() * (float) Math.PI / 180.0F);
            this.motionX += (double) (strafe * f2 - forward * f1);
            this.motionZ += (double) (forward * f2 + strafe * f1);
        }
    }

    private void jump() {
        this.motionY = 0.42F;

        val jump = PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.JUMP);
        if (jump > 0) {
            this.motionY += jump * 0.1F;
        }

        if (getData().getActionProcessor().isSprinting()) {
            float f = getData().getMovementProcessor().getTo().getYaw() * 0.017453292F;
            this.motionX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motionZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
    }
}
