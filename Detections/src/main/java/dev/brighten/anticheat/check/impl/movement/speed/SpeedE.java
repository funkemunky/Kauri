package dev.brighten.anticheat.check.impl.movement.speed;

import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.DevStage;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (E)", description = "Motion prediction detection", devStage = DevStage.RELEASE,
        punishVL = 15, vlToFlag = 1)
public class SpeedE extends Check {
    private boolean lastLastClientGround;
    private float buffer, lfriction;

    private static boolean[] TRUE_FALSE = new boolean[] {true, false};
    private static float[] VALUES = new float[] {-0.98f, 0f, 0.98f};

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {

        Block underBlock = BlockUtils.getBlock(data.playerInfo.to
                .toLocation(data.getPlayer().getWorld())
                .clone().subtract(0, 1, 0));

        if(underBlock == null) return;

        float friction = MinecraftReflection.getFriction(underBlock);

        debug("friction=%.3f material=%s", friction, underBlock.getType().name());

        check: {
            if(!packet.isPos()
                    || data.playerInfo.generalCancel
                    || data.playerInfo.onLadder
                    || data.playerInfo.lastVelocity.isNotPassed(1)
                    || data.blockInfo.inLiquid
                    || data.blockInfo.collidesHorizontally) break check;
            double smallestDelta = Double.MAX_VALUE;

            double pmotionx = 0, pmotionz = 0;
            boolean onGround = data.playerInfo.lClientGround;
            for(int f = -1 ; f < 2 ; f++) {
                for(int s = -1; s < 2 ; s++) {
                    for(boolean sprinting : TRUE_FALSE) {
                        for(boolean fastMath : TRUE_FALSE) {
                            for(boolean attack : TRUE_FALSE) {
                                for(boolean using : TRUE_FALSE) {
                                    for (boolean sneaking : TRUE_FALSE) {
                                        for (boolean jumped : TRUE_FALSE) {

                                            //Skipping impossible scenarios
                                            if ((f <= 0 && sprinting)
                                                    || (sprinting && sneaking)) continue;

                                            float forward = f, strafe = s;

                                            if (sneaking) {
                                                forward *= 0.3;
                                                strafe *= 0.3;
                                            }

                                            if(using) {
                                                forward*= 0.2;
                                                strafe*= 0.2;
                                            }

                                            //Multiplying by 0.98 like in client
                                            forward*= 0.98;
                                            strafe*= 0.98;

                                            float aiMoveSpeed = data.getPlayer().getWalkSpeed() / 2f;
                                            float drag = 0.91f;
                                            double lmotionX = data.playerInfo.lDeltaX, lmotionZ = data.playerInfo.lDeltaZ;

                                            //The "1" will effectively remove lastFriction from the equation
                                            lmotionX *= (lastLastClientGround ? lfriction: 1) * 0.91;
                                            lmotionZ *= (lastLastClientGround ? lfriction : 1) * 0.91;

                                            //Running multiplication done after previous prediction
                                            if (data.playerVersion.isOrAbove(ProtocolVersion.V1_9)) {
                                                if (Math.abs(lmotionX) < 0.003)
                                                    lmotionX = 0;
                                                if (Math.abs(lmotionZ) < 0.003)
                                                    lmotionZ = 0;
                                            } else {
                                                if (Math.abs(lmotionX) < 0.005)
                                                    lmotionX = 0;
                                                if (Math.abs(lmotionZ) < 0.005)
                                                    lmotionZ = 0;
                                            }

                                            // Attack slowdown
                                            if(attack) {
                                                lmotionX*= 0.6;
                                                lmotionZ*= 0.6;
                                            }

                                            if (sprinting) aiMoveSpeed += aiMoveSpeed * 0.3f;

                                            if (data.potionProcessor.hasPotionEffect(PotionEffectType.SPEED))
                                                aiMoveSpeed += (data.potionProcessor.getEffectByType(PotionEffectType.SPEED)
                                                        .get()
                                                        .getAmplifier() + 1) * (double) 0.2f * aiMoveSpeed;
                                            if (data.potionProcessor.hasPotionEffect(PotionEffectType.SLOW))
                                                aiMoveSpeed += (data.potionProcessor.getEffectByType(PotionEffectType.SLOW)
                                                        .get()
                                                        .getAmplifier() + 1) * (double) -0.15f * aiMoveSpeed;

                                            float f5;
                                            if (onGround) {
                                                drag *= friction;

                                                f5 = aiMoveSpeed * (0.16277136F / MiscUtils.pow(drag, 3));

                                                if (sprinting && jumped) {
                                                    float rot = data.playerInfo.to.yaw * 0.017453292F;
                                                    lmotionX -= sin(fastMath, rot) * 0.2F;
                                                    lmotionZ += cos(fastMath, rot) * 0.2F;
                                                }

                                            } else f5 = sprinting ? 0.026f : 0.02f;

                                            double keyedMotion = forward * forward + strafe * strafe;

                                            if (keyedMotion >= 1.0E-4F) {
                                                keyedMotion = f5 / Math.max(1.0, Math.sqrt(keyedMotion));
                                                forward *= keyedMotion;
                                                strafe *= keyedMotion;

                                                final float yawSin = sin(fastMath,
                                                        data.playerInfo.to.yaw * (float) Math.PI / 180.F),
                                                        yawCos = cos(fastMath,
                                                                data.playerInfo.to.yaw * (float) Math.PI / 180.F);

                                                lmotionX += (strafe * yawCos - forward * yawSin);
                                                lmotionZ += (forward * yawCos + strafe * yawSin);
                                            }

                                            double delta = Math.pow(data.playerInfo.deltaX - lmotionX, 2)
                                                    + Math.pow(data.playerInfo.deltaZ - lmotionZ, 2);

                                            if (delta < smallestDelta) {
                                                smallestDelta = delta;
                                                pmotionx = lmotionX;
                                                pmotionz = lmotionZ;
                                            }
                                            smallestDelta = Math.min(delta, smallestDelta);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            double pmotion = Math.hypot(pmotionx, pmotionz);

            if(data.playerInfo.deltaXZ > pmotion && smallestDelta > 1E-4 && data.playerInfo.deltaXZ > 0.1) {
               if(++buffer > 3) {
                   buffer = Math.min(3.5f, buffer); //Ensuring we don't have a run-away buffer
                   vl++;
                   flag("d=%.4f pm=%.3f dx=%.3f", smallestDelta, pmotion, data.playerInfo.deltaXZ);
               }
            } else if(buffer > 0) buffer-= 0.1f;

            debug("smallest=%s b=%.1f", smallestDelta, buffer);
        }

        lfriction = friction;
        lastLastClientGround = data.playerInfo.lClientGround;
    }

    private static final float[] SIN_TABLE_FAST = new float[4096];
    private static final float[] SIN_TABLE = new float[65536];

    private static float sin(boolean fastMath, float p_76126_0_)
    {
        return fastMath ? SIN_TABLE_FAST[(int)(p_76126_0_ * 651.8986F) & 4095] : SIN_TABLE[(int)(p_76126_0_ * 10430.378F) & 65535];
    }

    public static float cos(boolean fastMath, float value)
    {
        return fastMath ? SIN_TABLE_FAST[(int)((value + ((float)Math.PI / 2F)) * 651.8986F) & 4095] : SIN_TABLE[(int)(value * 10430.378F + 16384.0F) & 65535];
    }

    static {
        for (int i = 0; i < 65536; ++i)
        {
            SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
        }

        for (int j = 0; j < 4096; ++j)
        {
            SIN_TABLE_FAST[j] = (float)Math.sin((double)(((float)j + 0.5F) / 4096.0F * ((float)Math.PI * 2F)));
        }

        for (int l = 0; l < 360; l += 90)
        {
            SIN_TABLE_FAST[(int)((float)l * 11.377778F) & 4095] = (float)Math.sin((double)((float)l * 0.017453292F));
        }
    }


}
