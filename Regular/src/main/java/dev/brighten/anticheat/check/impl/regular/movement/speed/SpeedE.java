package dev.brighten.anticheat.check.impl.regular.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.math.DoubleValue;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Speed (E)", description = "Motion check", checkType = CheckType.SPEED,
        devStage = DevStage.ALPHA, punishVL = 12)
@Cancellable
public class SpeedE extends Check {

    private double threshold;
    private WrappedInFlyingPacket lastFlying, lastLastLast;

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        check: {
            if (!packet.isPos()) break check;

            if (lastFlying == null
                    || lastLastLast == null || !packet.isPos()
                    || !lastFlying.isPos() || !lastLastLast.isPos()
                    || !data.playerInfo.worldLoaded
                    || data.playerInfo.moveTicks < 5
                    || !data.playerInfo.checkMovement) {
                break check;
            }

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

            if ((testCollision(minX) || this.testCollision(minZ)
                    || this.testCollision(maxX) || this.testCollision(maxZ))) {
                this.threshold -= this.threshold > 0 ? .25 : 0;
                break check;
            }

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED))
                attributeSpeed += data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                        .map(pe -> pe.getAmplifier() + 1).orElse(0) * 0.2D * attributeSpeed;

            if(data.potionProcessor.hasPotionEffect(PotionEffectType.SLOW))
                attributeSpeed += data.potionProcessor.getEffectByType(PotionEffectType.SLOW)
                        .map(pe -> pe.getAmplifier() + 1).orElse(0) * -.15D * attributeSpeed;

            Motion predicted;
            double smallest = java.lang.Double.MAX_VALUE;

            iteration:
            {

                // Yes this looks retarded but its brute forcing every possible thing.
                for (int f = -1; f < 2; f++) {
                    for (int s = -1; s < 2; s++) {
                        for (int sp = 0; sp < 2; sp++) {
                            for (int jp = 0; jp < 2; jp++) {
                                for (int ui = 0; ui < 2; ui++) {
                                    for (int hs = 0; hs < 2; hs++) {
                                        for (int sn = 0; sn < 2; sn++) {

                                            final boolean sprint = sp == 0;
                                            final boolean jump = jp == 0;
                                            final boolean using = ui == 0;
                                            final boolean hitSlowdown = hs == 0;

                                            final boolean ground = lastFlying.isGround();
                                            final boolean sneaking = sn == 0;

                                            float forward = f;
                                            float strafe = s;

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

                                            if (lastLastLast.isGround()) {
                                                motion.getMotionX().multiply(0.6F * 0.91F);
                                                motion.getMotionZ().multiply(0.6F * 0.91F);
                                            } else {
                                                motion.getMotionX().multiply(0.91F);
                                                motion.getMotionZ().multiply(0.91F);
                                            }

                                            if (hitSlowdown) {
                                                motion.getMotionX().multiply(0.6D);
                                                motion.getMotionZ().multiply(0.6D);
                                            }

                                            motion.round();

                                            if (jump && sprint) {
                                                final float radians = data.playerInfo.to.yaw
                                                        * 0.017453292F;

                                                motion.getMotionX().subtract(MathHelper.sin(radians) * 0.2F);
                                                motion.getMotionZ().add(MathHelper.cos(radians) * 0.2F);
                                            }

                                            float slipperiness = 0.91F;
                                            if (ground) slipperiness = 0.6F * 0.91F;

                                            float moveSpeed = (float) attributeSpeed;
                                            if (sprint) moveSpeed += moveSpeed * 0.30000001192092896D;

                                            final float moveFlyingFriction;

                                            if (ground) {
                                                final float moveSpeedMultiplier = 0.16277136F /
                                                        (slipperiness * slipperiness * slipperiness);

                                                moveFlyingFriction = moveSpeed * moveSpeedMultiplier;
                                            } else {
                                                moveFlyingFriction = (float)
                                                        (sprint ? ((double) 0.02F + (double) 0.02F * 0.3D) : 0.02F);
                                            }

                                            motion.apply(this.moveFlying(
                                                    forward, strafe,
                                                    moveFlyingFriction
                                            ));

                                            motion.getMotionY().set(0.0);

                                            final double distance = realMotion.distanceSquared(motion);

                                            if (distance < smallest) {
                                                smallest = distance;
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

            if (data.playerInfo.lastTeleportTimer.isNotPassed(2)) break check;

            if (smallest > 1E-6
                    && data.playerInfo.deltaXZ > .2 && !this.ignore()) {

                if (this.threshold++ > 12) {
                    vl++;
                    this.flag("offset=" + smallest);
                }
            } else {
                this.threshold -= this.threshold > 0 ? .005 : 0;
            }
        }

        lastLastLast = lastFlying;
        lastFlying = packet;

    }



    @Getter
    public static final class Motion {

        private DoubleValue motionX, motionY, motionZ;

        /**
         * Create an empty constructor if we do not want initial values for our motion.
         */
        public Motion() {

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

        public double distanceSquared(final Motion other) {
            return Math.pow(this.motionX.get() - other.getMotionX().get(), 2) +
                    Math.pow(this.motionY.get() - other.getMotionY().get(), 2) +
                    Math.pow(this.motionZ.get() - other.getMotionZ().get(), 2);
        }

        public Motion clone() {
            return new Motion(motionX.get(), motionY.get(), motionZ.get());
        }
    }

    private MoveFlyingResult moveFlying(final float moveForward, final float moveStrafe, final float friction) {
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

            final float f1 = MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F);
            final float f2 = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F);

            final float factorX = strafe * f2 - forward * f1;
            final float factorZ = forward * f2 + strafe * f1;

            moveFlyingFactorX = factorX;
            moveFlyingFactorZ = factorZ;
        }

        return new MoveFlyingResult(moveFlyingFactorX, moveFlyingFactorZ);
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
                || data.playerInfo.slimeTimer.isNotPassed(2)
                || data.playerInfo.lastVelocity.isNotPassed(5)
                || data.playerInfo.iceTimer.isNotPassed(2)
                || data.playerInfo.blockAboveTimer.isNotPassed(3);
    }

    boolean testCollision(double value) {
        return Math.abs(value % 0.015625D) < 1E-10;
    }
}
