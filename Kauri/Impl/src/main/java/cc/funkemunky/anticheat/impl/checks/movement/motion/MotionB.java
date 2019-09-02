package cc.funkemunky.anticheat.impl.checks.movement.motion;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.*;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

@Init
@CheckInfo(name = "Motion (Type B)", description = "Predicts the movement of a player and ensures is legitimate.", type = CheckType.MOTION, maxVL = 50, enabled = false)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.FLYING, Packet.Client.LOOK, Packet.Server.ENTITY_VELOCITY})
public class MotionB extends Check {

    private float motionX, motionY, motionZ;
    private boolean isAirBorne, tookVelocity;
    private float vl;
    private Vector velocity;
    private long velocityTimestamp;

    private static List<float[]> motions = Collections.synchronizedList(Arrays.asList(
            new float[] {0, 0},
            new float[] {0, .98f},
            new float[] {0, -.98f},
            new float[] {.98f, 0},
            new float[] {.98f, .98f},
            new float[] {.98f, -.98f},
            new float[] {-.98f, 0},
            new float[] {-.98f, .98f},
            new float[] {-.98f, -.98f}));

    //TODO Debug and test.
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

            if(!getData().isGeneralCancel() && move.getHalfBlockTicks() == 0 && move.getLiquidTicks() == 0 && !move.isBlocksOnTop() && !move.isCollidesHorizontally() && !getData().getActionProcessor().isUsingItem()) {
                if(move.isServerOnGround() && move.getTo().getY() % 1 == 0 && move.getDeltaY() < 0 && move.getDeltaY() > motionY) motionY = move.getDeltaY();
                val predicted = new Vector(motionX, motionY, motionZ);
                val to = new Vector(move.getDeltaX(), move.getDeltaY(), move.getDeltaZ());

                val distance = predicted.distance(to);

                if(distance > (tookVelocity ? 0.55 : 0.1)) {
                    float predictedXZ = (float) MathUtils.hypot(predicted.getX(), predicted.getZ()), predictedY = (float) predicted.getY();
                    vl= vl < 40 ? vl + 1 : vl;
                    if(vl > 20) {
                        flag("vl=" + vl + " predictedXZ=" + predictedXZ + " predictedY=" + predictedY + " deltaXZ=" + move.getDeltaXZ() + " deltaY=" + move.getDeltaY(), true, true, AlertTier.HIGH);
                    }
                    debug(Color.Green + "Flag: " + distance + " vl=" + vl + " predicted=" + predictedXZ + " deltaXZ=" + move.getDeltaXZ());
                } else {
                    vl-= vl > 0 ? 1 : 0;
                    if(move.getDeltaXZ() > 0) debug("distance=" + distance + " vl=" + vl);
                }
            } else vl -= vl > 0 ? 0.5 : 0;

            moveEntityWithHeading(0,0, true);

            if(move.isServerPos()) motionX = motionY = motionZ = 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }


    private void runMovement() {
        val move = getData().getMovementProcessor();

        val to = new Vector(move.getDeltaX(), move.getDeltaY(), move.getDeltaZ());

        if(velocity != null && MathUtils.millisToTicks(System.currentTimeMillis() - velocityTimestamp) > MathUtils.millisToTicks(getData().getTransPing())) {
            motionX = (float) velocity.getX();
            motionY = (float) velocity.getY();
            motionZ = (float) velocity.getZ();
            tookVelocity = true;
            velocity = null;
        }

        if(Math.abs(motionX) < 0.005) motionX = 0;
        if(Math.abs(motionY) < 0.005) motionY = 0;
        if(Math.abs(motionZ) < 0.005) motionZ = 0;

        float bmotionx = motionX, bmotiony = motionY, bmotionz = motionZ;
        boolean lastAirborne = isAirBorne;

        List<Vector> vectors = new ArrayList<>();

        for (float[] array : motions) {
            moveEntityWithHeading(array[0], array[1], false);

            if(!isAirBorne && MathUtils.approxEquals(0.01, move.getDeltaY(), cc.funkemunky.anticheat.api.utils.MiscUtils.getPredictedJumpHeight(getData()))) {
                jump();
            }

            vectors.add(new Vector(motionX, motionY, motionZ));

            motionX = bmotionx;
            motionY = bmotiony;
            motionZ = bmotionz;
            isAirBorne = lastAirborne;
        }

        val optional = vectors.parallelStream().min(Comparator.comparing(vec -> vec.distance(to)));

        if(optional.isPresent()) {
            Vector vec = optional.get();
            motionX = (float) vec.getX();
            motionY = (float) vec.getY();
            motionZ = (float) vec.getZ();
        }

        if(move.isClientOnGround()) {
            isAirBorne = tookVelocity = false;
        }
    }
    private void moveEntityWithHeading(float strafe, float forward, boolean after) {
        val move = getData().getMovementProcessor();
        float f4 = 0.91F;

        if (move.isClientOnGround()) {
            Block below = BlockUtils.getBlock(move.getTo().toLocation(getData().getPlayer().getWorld()).subtract(0, 0.5f, 0));
            f4 = (below != null && below.getType().isSolid() ? ReflectionsUtil.getFriction(getData().getBlockBelow()) : 0.68f) * 0.91F;
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
            float f1 = MathHelper.sin(MathUtils.yawTo180F(move.getFrom().getYaw()) * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(MathUtils.yawTo180F(move.getFrom().getYaw()) * (float) Math.PI / 180.0F);
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
            float f = MathUtils.yawTo180F(getData().getMovementProcessor().getFrom().getYaw()) * 0.017453292F;
            this.motionX -= (double) (MathHelper.sin(f) * 0.2F);
            this.motionZ += (double) (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
    }
}
