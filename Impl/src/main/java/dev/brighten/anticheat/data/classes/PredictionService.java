package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.FastTrig;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

public class PredictionService {

    private final ObjectData data;
    public String key = "Nothing";
    public double forward, strafe;
    private double x, z, motionX, motionZ, lmotionX, lmotionZ;
    private float yaw, pitch;
    public float motionYaw;
    public double pMotionX, pMotionZ, aiMoveSpeed, drag;
    public boolean sprinting, sneaking, hit, velocity, lvelocity, usingItem, dropItem, underProblemBlock, error;
    private boolean lSprinting, onGround;

    public PredictionService(ObjectData data) {
        this.data = data;
    }

    public void onReceive(WrappedInFlyingPacket packet) {
        error = true; //Will be stay true if something went wrong

        if(data.playerInfo.to == null || data.playerInfo.from == null) return;

        //Updating position and look values
        motionX = data.playerInfo.deltaX;
        motionZ = data.playerInfo.deltaZ;
        x = data.playerInfo.to.x;
        z = data.playerInfo.to.z;
        yaw = data.playerInfo.to.yaw;
        pitch = data.playerInfo.to.pitch;
        onGround = data.playerInfo.lClientGround;

        //Calculations
        if(packet.isPos()) {
            //Checking for other conditions that may cause us problems
            if(velocity) {
                lmotionX = data.playerInfo.velocityX;
                lmotionZ = data.playerInfo.velocityZ;
            }
            if(!velocity
                    && !data.blockInfo.inLiquid
                    && !data.playerInfo.onLadder
                    //If player is not moving but sending positions anyway
                    && !(motionX == 0 && motionZ == 0 && onGround)
                    //When a player jumps, we don't want to check since we don't have math for it and checking for jump
                    //can be unreliable
                    && !(!data.playerInfo.clientGround && data.playerInfo.lClientGround && (lSprinting || sprinting))) {
                if(lSprinting && hit) {
                    lmotionX*= 0.6;
                    lmotionZ*= 0.6;
                }
                //Calculating what key the player may be pressing
                calcKey(motionX - lmotionX, motionZ - lmotionZ);
                calculateNewPosition();

            }

            //Setting boolean values back to false after they have been used
            lvelocity = velocity;
            velocity = false;
        } else {
            key = "";
            forward = strafe = 0;
        }

        //Miscellanious caveat updating
        if(dropItem) usingItem = dropItem = false;

        //lMotion calculations
        lmotionX = motionX;
        lmotionZ = motionZ;

        if(!error) {
            lmotionX*= drag;
            lmotionZ*= drag;
        }

        //Setting previous value stuffs
        lSprinting = sprinting;
        hit = false;
    }

    private void calculateNewPosition() {
        int precision = String.valueOf((int) Math.abs(x)).length();
        precision = 10 - precision - (underProblemBlock ? 4 : 0);

        calculation: {
            float cstrafe = (float)strafe, cforward = (float)forward;

            if(sneaking) {
                cstrafe*= .3;
                cforward*= .3;
            }

            drag = 0.91;

            //Getting the friction of the player's block below if they are on ground.

            aiMoveSpeed = data.getPlayer().getWalkSpeed() / 2f;

            if(sprinting) aiMoveSpeed+= aiMoveSpeed * 0.3f;

            data.potionProcessor.getEffectByType(PotionEffectType.SPEED).ifPresent(speed ->
                    aiMoveSpeed+= (speed.getAmplifier() + 1) * (double)0.2f * aiMoveSpeed);
            data.potionProcessor.getEffectByType(PotionEffectType.SLOW).ifPresent(speed ->
                    aiMoveSpeed+= (speed.getAmplifier() + 1) * (double)-0.15f * aiMoveSpeed);

            double motionForward;

            if(onGround) {
                Block underBlock = BlockUtils.getBlock(data.playerInfo.to
                        .toLocation(data.getPlayer().getWorld())
                        .clone().subtract(0, 1, 0));

                if(underBlock == null)
                    break calculation;

                drag*= MinecraftReflection.getFriction(underBlock);

                motionForward = aiMoveSpeed * (0.16277136F / Math.pow(drag, 3));
            } else motionForward = lSprinting ? 0.026f : 0.02f;

            double keyedMotion = cforward * cforward + cstrafe + cstrafe;

            if(keyedMotion >= 1.0E-4F) {
                keyedMotion = motionForward / Math.min(1.0, Math.sqrt(keyedMotion));
                cforward*= keyedMotion;
                cstrafe*= keyedMotion;

                final double yawSin = Math.sin(yaw * Math.PI / 180.F), yawCos = Math.cos(yaw * Math.PI / 180.F);

                pMotionX = lmotionX + (cstrafe * yawCos - cforward * yawSin);
                pMotionZ = lmotionZ + (cforward * yawCos - cstrafe * yawSin);
            }

            error = false;
        }
    }


    private float getMotionYaw(double mx, double mz) {
        float motionYaw = (float) (FastTrig.fast_atan2(mz, mx) * 180.0D / Math.PI) - 90.0F; // is the rotationYaw from the Motion
        // of the Player

        motionYaw -= yaw;

        while (motionYaw > 360.0F)
            motionYaw -= 360.0F;
        while (motionYaw < 0.0F)
            motionYaw += 360.0F;

        return motionYaw;
    }

    private void calcKey(double mx, double mz) {
        float motionYaw = getMotionYaw(mx, mz);

        this.motionYaw = motionYaw;

        int direction = 6;

        //MiscUtils.testMessage("yaw= " + motionYaw + " mx=" + mx + " mz=" + mz);

        motionYaw /= 45.0F; // converts the rotationYaw of the Motion to integers to get keys

        float moveS = 0.0F; // is like the ClientSide strafe forward
        float moveF = 0.0F;
        String key = "Nothing";

        double preD = 1.2 * Math.pow(10, -3);

        if (Math.abs(Math.abs(mx) + Math.abs(mz)) > preD) {
            direction = Math.round(motionYaw);

            if (direction == 1) {
                moveF = 1F;
                moveS = -1F;
                key = "W + D";
            } else if (direction == 2) {
                moveS = -1F;
                key = "D";
            } else if (direction == 3) {
                moveF = -1F;
                moveS = -1F;
                key = "S + D";
            } else if (direction == 4) {
                moveF = -1F;
                key = "S";
            } else if (direction == 5) {
                moveF = -1F;
                moveS = 1F;
                key = "S + A";
            } else if (direction == 6) {
                moveS = 1F;
                key = "A";
            } else if (direction == 7) {
                moveF = 1F;
                moveS = 1F;
                key = "W + A";
            } else if (direction == 8) {
                moveF = 1F;
                key = "W";
            } else if (direction == 0) {
                moveF = 1F;
                key = "W";
            }
        }

        moveF *= 0.9800000190734863F;
        moveS *= 0.9800000190734863F;

        strafe = moveS;
        forward = moveF;
        this.key = key;

        /*if(data.getPlayer().getName().equals("Dogeritoz")) {
            Bukkit.broadcastMessage("key=" + key + " mx=" + MathUtils.round(mx, 6)
                    + " mz=" + MathUtils.round(mz, 6) + " myaw=" + MathUtils.round(motionYaw, 4)
                    + " velocity=" + velocity + " vx=" + data.playerInfo.velocityX + " vz=" + data.playerInfo.velocityZ);
        }*/
    }
}