package dev.brighten.anticheat.check.impl.regular.movement.speed;

import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.math.DoubleValue;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@CheckInfo(name = "Speed (E)", description = "Motion check - Rhys", checkType = CheckType.SPEED,
        devStage = DevStage.ALPHA, punishVL = 30)
@Cancellable
public class SpeedE extends Check {

    private double threshold;
    private WrappedInFlyingPacket lastFlying, lastLastLast;
    private Timer flyingTimer = new MillisTimer(), transactionTimer = new MillisTimer();

    private static final boolean[] TRUE_FALSE = new boolean[]{true, false};

    @Packet
    public void onTransaction(WrappedInTransactionPacket packet) {
        transactionTimer.isPassed();
    }

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        check:
        {
            if (!packet.isPos()) break check;

            if (lastFlying == null
                    || lastLastLast == null || !packet.isPos()
                    // If previous flyings aren't positions, can cause accuracy issues. Client can
                    // truncate distances if they're too small (bad for us).
                    || !lastFlying.isPos() || !lastLastLast.isPos()
                    || !data.playerInfo.worldLoaded
                    // If blockBelow is null, we will not be able to account for movement on ground.
                    || data.playerInfo.blockBelow == null
                    || data.playerInfo.moveTicks < 5 // If player recently teleported
                    // Velocity isn't accounted for right now due to reliance on client-server interactions.
                    || data.playerInfo.lastVelocity.isNotPassed(2)
                    // Server wouldn't be checking their movement in this instance
                    || !data.playerInfo.checkMovement
                    // Do not check on recent teleports
                    || data.playerInfo.lastTeleportTimer.isNotPassed(1)) {
                break check;
            }

            // 1.9 checking for lack of positional sending
            if(data.playerVersion.isOrAbove(ProtocolVersion.V1_9) &&
                transactionTimer.isNotPassed(100L) && flyingTimer.isPassed(100L)) {
                break check;
            }

            // Ladders change the horizontal movement.
            if (data.playerInfo.climbTimer.isNotPassed(3)) {
                this.threshold -= this.threshold > 0 ? .3 : 0;
                break check;
            }

            final Motion realMotion = new Motion(data.playerInfo.deltaX,
                    0.0D, data.playerInfo.deltaZ);

            double attributeSpeed = data.getPlayer().getWalkSpeed() / 2;

            final SimpleCollisionBox boundingBox = new SimpleCollisionBox((float) packet.getX() - 0.3F,
                    (float) packet.getY(), (float) packet.getZ() - 0.3F,
                    (float) packet.getX() + 0.3F, (float) packet.getY() + 1.8F,
                    (float) packet.getZ() + 0.3F);

            final double minX = boundingBox.xMin;
            final double minZ = boundingBox.zMin;

            final double maxX = boundingBox.xMax;
            final double maxZ = boundingBox.zMax;

            // If the player is interacting with blocks horizontally, it will cause false positives
            // as we do not account for block collisions in distance yet.
            if ((testCollision(minX) || this.testCollision(minZ)
                    || this.testCollision(maxX) || this.testCollision(maxZ))) {
                this.threshold -= this.threshold > 0 ? .25 : 0;
                break check;
            }

            if (data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED))
                attributeSpeed += data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                        .map(pe -> pe.getAmplifier() + 1).orElse(0) * 0.20000000298023224 * attributeSpeed;

            if (data.potionProcessor.hasPotionEffect(PotionEffectType.SLOW))
                attributeSpeed += data.potionProcessor.getEffectByType(PotionEffectType.SLOW)
                        .map(pe -> pe.getAmplifier() + 1).orElse(0) * -0.15000000596046448 * attributeSpeed;

            Motion predicted = null;
            double smallest = java.lang.Double.MAX_VALUE;

            int jumpAmp = data.potionProcessor.getEffectByType(PotionEffectType.JUMP).map(ef -> ef.getAmplifier() + 1)
                    .orElse(0);

            iteration:
            {
                // Yes this looks retarded but its brute forcing every possible thing.
                for (int f = -1; f < 2; f++) {
                    for (int s = -1; s < 2; s++) {
                        for (boolean fastMath : TRUE_FALSE) {
                            for (boolean sprint : TRUE_FALSE) {
                                for (boolean jump : TRUE_FALSE) {
                                    for (boolean using : TRUE_FALSE) {
                                        for (boolean hitSlowdown : TRUE_FALSE) {
                                            for (boolean sneaking : TRUE_FALSE) {

                                                final boolean ground = lastFlying.isGround();

                                                float forward = f;
                                                float strafe = s;

                                                //Impossible to sprint while not going forward
                                                if (forward <= 0) sprint = false;

                                                if (using) {
                                                    forward *= 0.2D;
                                                    strafe *= 0.2D;
                                                }

                                                if (sneaking) {
                                                    forward *= (float) 0.3D;
                                                    strafe *= (float) 0.3D;
                                                }

                                                forward *= 0.98F;
                                                strafe *= 0.98F;

                                                final Motion motion = new Motion(
                                                        data.playerInfo.lDeltaX,
                                                        0.0D,
                                                        data.playerInfo.lDeltaZ
                                                );

                                                if (data.playerVersion.isBelow(ProtocolVersion.V1_9))
                                                    motion.round();
                                                else motion.round19();

                                                if (lastLastLast.isGround()) {
                                                    motion.getMotionX().multiply(0.6F * 0.91F);
                                                    motion.getMotionZ().multiply(0.6F * 0.91F);
                                                } else {
                                                    motion.getMotionX().multiply(0.91F);
                                                    motion.getMotionZ().multiply(0.91F);

                                                    if(data.playerInfo.clientGround) {
                                                        motion.getMotionY().set(0);
                                                    } else {
                                                        motion.getMotionY().subtract(0.08);
                                                        motion.getMotionY().multiply(0.9800000190734863);
                                                    }
                                                }

                                                if (hitSlowdown) {
                                                    motion.getMotionX().multiply(0.6D);
                                                    motion.getMotionZ().multiply(0.6D);
                                                }

                                                if (ground && jump && sprint) {
                                                    final float radians = data.playerInfo.to.yaw
                                                            * 0.017453292F;

                                                    motion.getMotionX()
                                                            .subtract(sin(fastMath, radians) * 0.2F);
                                                    motion.getMotionZ()
                                                            .add(cos(fastMath, radians) * 0.2F);
                                                    motion.getMotionY().set(0.42f + (jumpAmp * 0.1));
                                                }

                                                float slipperiness = 0.91F;
                                                if (ground) slipperiness = MinecraftReflection
                                                        .getFriction(data.playerInfo.blockBelow) * 0.91F;

                                                float moveSpeed = (float) attributeSpeed;
                                                if (sprint) moveSpeed += moveSpeed * 0.30000001192092896D;

                                                final float moveFlyingFriction;

                                                if (ground) {
                                                    final float moveSpeedMultiplier = 0.16277136F /
                                                            (slipperiness * slipperiness * slipperiness);

                                                    moveFlyingFriction = moveSpeed * moveSpeedMultiplier;
                                                } else {
                                                    moveFlyingFriction = (float)
                                                            (sprint ? ((double) 0.02F
                                                                    + (double) 0.02F * 0.3D) : 0.02F);
                                                }

                                                motion.apply(this.moveFlying(fastMath,
                                                        forward, strafe,
                                                        moveFlyingFriction
                                                ));

                                                //Web math
                                                if(data.blockInfo.inWeb) {
                                                    motion.motionX.multiply(0.25);
                                                    motion.motionY.multiply(0.05000000074505806);
                                                    motion.motionZ.multiply(0.25);
                                                } else if(data.blockInfo.onSoulSand) {
                                                    motion.motionX.multiply(0.4);
                                                    motion.motionZ.multiply(0.4);
                                                }

                                                double motionY = motion.motionY.get();
                                                motion.motionY.set(0);
                                                final double distance = realMotion.distanceSquared(motion);

                                                if (distance < smallest) {
                                                    smallest = distance;
                                                    motion.motionY.set(motionY);
                                                    predicted = motion;

                                                    if (distance < 1E-8) {
                                                        break iteration;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (smallest > 1E-6
                    && data.playerInfo.deltaXZ > .2 && !this.ignore()) {

                if (this.threshold++ > 5) {
                    vl++;
                    this.flag("offset=" + smallest);
                }
            } else {
                this.threshold -= this.threshold > 0 ? .005 : 0;
            }

            if(predicted != null)
            debug("(%.2f) o=%s dy=%.4f py=%.4f", threshold, smallest, data.playerInfo.deltaY,
                    predicted.getMotionY());
            else debug("predicted was null");
        }

        lastLastLast = lastFlying;
        lastFlying = packet;
        flyingTimer.reset();
    }


    @Getter
    public static final class Motion {

        private DoubleValue motionX, motionY, motionZ;

        /**
         * Create an empty constructor if we do not want initial values for our motion.
         */
        public Motion() {

        }

        public Motion(Vector vector) {
            this(vector.getX(), vector.getY(), vector.getZ());
        }

        /**
         * Set an initial value for our base motion.
         */
        public Motion(final double x, final double y, final double z) {
            this.motionX = new DoubleValue(x);
            this.motionY = new DoubleValue(y);
            this.motionZ = new DoubleValue(z);
        }

        /**
         * Set an initial value for our base motion.
         */
        public Motion(final DoubleValue motionX, final DoubleValue motionY, final DoubleValue motionZ) {
            this.motionX = new DoubleValue(motionX.get());
            this.motionY = new DoubleValue(motionY.get());
            this.motionZ = new DoubleValue(motionZ.get());
        }

        public void set(final Vector vector) {
            this.motionX.set(vector.getX());
            this.motionY.set(vector.getY());
            this.motionZ.set(vector.getZ());
        }

        public void add(final Vector vector) {
            this.motionX.add(vector.getX());
            this.motionY.add(vector.getY());
            this.motionZ.add(vector.getZ());
        }

        public void apply(final MoveFlyingResult moveFlyingResult) {
            this.motionX.add(moveFlyingResult.getX());
            this.motionZ.add(moveFlyingResult.getZ());
        }

        public void round() {
            if (Math.abs(this.motionX.get()) < 0.005D) this.motionX.set(0.0D);
            if (Math.abs(this.motionY.get()) < 0.005D) this.motionY.set(0.0D);
            if (Math.abs(this.motionZ.get()) < 0.005D) this.motionZ.set(0.0D);
        }

        public void round19() {
            if (Math.abs(this.motionX.get()) < 0.003D) this.motionX.set(0.0D);
            if (Math.abs(this.motionY.get()) < 0.003D) this.motionY.set(0.0D);
            if (Math.abs(this.motionZ.get()) < 0.003D) this.motionZ.set(0.0D);
        }

        public double distanceSquared(final Motion other) {
            return Math.pow(this.motionX.get() - other.getMotionX().get(), 2) +
                    Math.pow(this.motionY.get() - other.getMotionY().get(), 2) +
                    Math.pow(this.motionZ.get() - other.getMotionZ().get(), 2);
        }

        public Motion clone() {
            return new Motion(motionX.get(), motionY.get(), motionZ.get());
        }
    }

    private MoveFlyingResult moveFlying(final boolean fastMath,
                                        final float moveForward, final float moveStrafe, final float friction) {
        float diagonal = moveStrafe * moveStrafe + moveForward * moveForward;

        float moveFlyingFactorX = 0.0F;
        float moveFlyingFactorZ = 0.0F;

        if (diagonal >= 1.0E-4F) {
            diagonal = MathHelper.c(diagonal);

            if (diagonal < 1.0F) {
                diagonal = 1.0F;
            }

            diagonal = friction / diagonal;

            final float strafe = moveStrafe * diagonal;
            final float forward = moveForward * diagonal;

            final float rotationYaw = data.playerInfo.to.yaw;

            final float f1 = sin(fastMath, rotationYaw * (float) Math.PI / 180.0F);
            final float f2 = cos(fastMath, rotationYaw * (float) Math.PI / 180.0F);

            final float factorX = strafe * f2 - forward * f1;
            final float factorZ = forward * f2 + strafe * f1;

            moveFlyingFactorX = factorX;
            moveFlyingFactorZ = factorZ;
        }

        return new MoveFlyingResult(moveFlyingFactorX, moveFlyingFactorZ);
    }

    private static final float[] SIN_TABLE_FAST = new float[4096];
    private static final float[] SIN_TABLE = new float[65536];

    private static float sin(boolean fastMath, float p_76126_0_) {
        return fastMath ? SIN_TABLE_FAST[(int) (p_76126_0_ * 651.8986F) & 4095]
                : SIN_TABLE[(int) (p_76126_0_ * 10430.378F) & 65535];
    }

    private static float cos(boolean fastMath, float p_76134_0_) {
        return fastMath ? SIN_TABLE_FAST[(int) ((p_76134_0_ + ((float) Math.PI / 2F)) * 651.8986F) & 4095]
                : SIN_TABLE[(int) (p_76134_0_ * 10430.378F + 16384.0F) & 65535];
    }

    static {
        int i;

        for (i = 0; i < 65536; ++i) {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }

        for (i = 0; i < 4096; ++i) {
            SIN_TABLE_FAST[i] = (float) Math.sin(((float) i + 0.5F) / 4096.0F * ((float) Math.PI * 2F));
        }

        for (i = 0; i < 360; i += 90) {
            SIN_TABLE_FAST[(int) ((float) i * 11.377778F) & 4095] = (float) Math
                    .sin((float) i * 0.017453292F);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class MoveFlyingResult {
        private final double x;
        private final double z;
    }

    boolean ignore() {
        return data.playerInfo.liquidTimer.isNotPassed(2)
                || data.blockInfo.collidesHorizontally
                || data.playerInfo.webTimer.isNotPassed(4)
                || data.playerInfo.blockAboveTimer.isNotPassed(3);
    }

    boolean testCollision(double value) {
        return Math.abs(value % 0.015625D) < 1E-10;
    }
}
