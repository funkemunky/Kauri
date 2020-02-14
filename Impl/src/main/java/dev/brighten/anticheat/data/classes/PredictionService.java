package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.reflections.impl.MinecraftReflection;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.world.BlockData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.MiscUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PredictionService {

    private ObjectData data;
    public boolean fly, velocity, position, sneak, sprint, useSword, hit, dropItem, inWeb, checkConditions,
            lastOnGround, onGround, lastSprint, fMath, fastMath, walkSpecial, lastVelocity;
    public double posX, posY, posZ, lPosX, lPosY, lPosZ, rmotionX, rmotionY, rmotionZ, lmotionX, lmotionZ, lmotionY;
    public double predX, predZ;
    public TickTimer lastUseItem = new TickTimer(10);
    public double aiMoveSpeed;
    public boolean collidedHorizontally, collidedVertically, flag, isBelowSpecial;
    public float walkSpeed, yaw, moveStrafing, moveForward;

    public String key = "Nothing";

    public PredictionService(ObjectData data) {
        this.data = data;
    }

    public void onReceive(WrappedInFlyingPacket packet) {

        inWeb = data.blockInfo.inWeb;

        if(packet.isLook()) {
            yaw = packet.getYaw();
        }

        if(packet.isPos()) {
            posX = packet.getX();
            posY = packet.getY();
            posZ = packet.getZ();
        } else {
            posX = 999999999;
            posY = 999999999;
            posZ = 999999999;
        }


        onGround = packet.isGround();

        boolean specialBlock = false;

        rmotionX = posX - lPosX;
        rmotionY = posY - lPosY;
        rmotionZ = posZ - lPosZ;

        Block blockBelow = BlockUtils.getBlock(new Location(data.getPlayer().getWorld(), MathHelper.floor_double(posX),
                MathHelper.floor_double(posY -  0.20000000298023224D), MathHelper.floor_double(posZ)));

        if(blockBelow != null) {
            isBelowSpecial = XMaterial.SLIME_BLOCK.parseMaterial().equals(blockBelow.getType())
                    || XMaterial.SOUL_SAND.parseMaterial().equals(blockBelow.getType());
        }

        //dev.brighten.anticheat.utils.MiscUtils.testMessage(Color.Gray + rmotionX + ", " + rmotionZ);
        fMath = fastMath; // if the Player uses Optifine FastMath

        try {
            if(!position && !velocity && (checkConditions = checkConditions(lastSprint))) {
                if (lastSprint && hit) { // If the Player Sprints and Hit a Player he get slowdown
                    lmotionX *= 0.6D;
                    lmotionZ *= 0.6D;
                }
                double mx = rmotionX - lmotionX; // mx, mz is an Value to calculate the rotation and the Key of the Player
                double mz = rmotionZ - lmotionZ;

                calcKey(mx, mz);

                //MiscUtils.testMessage("key: " + key);
                calc(true);
            }

            specialBlock = checkSpecialBlock(); // If the Player Walks on a Special block like Ice, Slime, Soulsand
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        if(dropItem) {
            useSword = false;
        }
        dropItem = false;

        if(blockBelow != null
                && onGround) {
            if(XMaterial.SLIME_BLOCK.parseMaterial().equals(blockBelow.getType())) {
                if(Math.abs(data.playerInfo.deltaY) < 0.1 && !data.playerInfo.sneaking) {
                    double shit = 0.4081 + Math.abs(data.playerInfo.deltaY) * 0.20000000298023224D;

                    rmotionZ *= shit;
                    rmotionX *= shit;
                    MiscUtils.testMessage("slime: " + shit + ", " + data.blockInfo.currentFriction);
                }
            } else if(XMaterial.SOUL_SAND.parseMaterial().equals(blockBelow.getType())) {
                double shit = 0.4;
                rmotionX *= shit;
                rmotionZ *= shit;
                MiscUtils.testMessage("soulsand: " + shit + ", " + data.blockInfo.currentFriction);
            }
        }

        if(data.blockInfo.inWeb) {
            rmotionX *= 0.25;
            rmotionZ *= 0.25;
        }

        double multiplier = 0.9100000262260437D; // multiplier = is the value that the client multiplies every move

        if(!data.blockInfo.inLiquid) {
            if (lastOnGround) {
               // multiplier = 0.60000005239967D;
                multiplier*= data.blockInfo.currentFriction;

               //MiscUtils.testMessage("friction: " + data.blockInfo.currentFriction);
            }
            rmotionX *= multiplier;
            rmotionZ *= multiplier;
        } else {
            if (data.blockInfo.inLava) {
                rmotionX *= 0.5f;
                rmotionZ *= 0.5f;
            } else if (data.blockInfo.inWater) {
                float f1 = 0.8f;
                float f2 = 0.02f;
                float f3 = PlayerUtils.getDepthStriderLevel(data.getPlayer());

                if (f3 > 0) {
                    f3 = 3.0f;
                }

                if (!lastOnGround) {
                    f3 *= .5f;
                }

                if (f3 > 0) {
                    f1 += (0.54600006F - f1) * f3 / 3.0F;
                }
                rmotionX *= f1;
                rmotionZ *= f1;
            }
        }

        if(ProtocolVersion.getGameVersion().isOrBelow(ProtocolVersion.V1_8_9)) {
            if (Math.abs(rmotionX) < 0.005D) // the client sets the motionX,Y and Z to 0 if its slower than 0.005D
                // because he would never stand still
                rmotionX = 0.0D;
            if (Math.abs(rmotionY) < 0.005D)
                rmotionY = 0.0D;
            if (Math.abs(rmotionZ) < 0.005D)
                rmotionZ = 0.0D;
        }

        // Saves the values for the next MovePacket

        lmotionX = rmotionX;
        lmotionY = rmotionY;
        lmotionZ = rmotionZ;

        lPosX = posX;
        lPosY = posY;
        lPosZ = posZ;

        hit = false;
        lastVelocity = velocity;
        velocity = false;
        position = false;

        lastOnGround = onGround;
        lastSprint = sprint;
        walkSpecial = specialBlock;
        fastMath = fMath;
    }

    private boolean checkSpecialBlock() {
        return (data.playerInfo.iceTimer.hasNotPassed(20)
                || data.playerInfo.soulSandTimer.hasNotPassed(20)
                || data.playerInfo.climbTimer.hasNotPassed(20) || data.playerInfo.wasOnSlime)
                && (onGround || lastOnGround);
    }

    private float getMotionYaw(double mx, double mz) {
        float motionYaw = (float) (Math.atan2(mz, mx) * 180.0D / Math.PI) - 90.0F; // is the rotationYaw from the Motion
        // of the Player

        motionYaw -= yaw;

        while (motionYaw > 360.0F)
            motionYaw -= 360.0F;
        while (motionYaw < 0.0F)
            motionYaw += 360.0F;

        return motionYaw;
    }
    private void calcKey(float motionYaw, double mx, double mz) {
        // of the Player

        int direction = 6;

        motionYaw -= yaw;

        while (motionYaw > 360.0F)
            motionYaw -= 360.0F;
        while (motionYaw < 0.0F)
            motionYaw += 360.0F;

        motionYaw /= 45.0F; // converts the rotationYaw of the Motion to integers to get keys

        float moveS = 0.0F; // is like the ClientSide moveStrafing moveForward
        float moveF = 0.0F;
        String key = "Nothing";

        int precision = String.valueOf((int) Math.abs(posX > posZ ? posX : posX)).length();
        precision = 15 - precision;
        double preD = 1.2 * Math.pow(10, -Math.max(3, precision - 5));

        if (Math.abs(Math.abs(mx) + Math.abs(mz)) > preD) {
            direction = (int) new BigDecimal(motionYaw).setScale(1, RoundingMode.HALF_UP).doubleValue();

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

        moveStrafing = moveS;
        moveForward = moveF;
        this.key = key;
    }

    private void calcKey(double mx, double mz) {
        float motionYaw = getMotionYaw(mx, mz);

        int direction = 6;

        //MiscUtils.testMessage("yaw= " + motionYaw + " mx=" + mx + " mz=" + mz);

        motionYaw /= 45.0F; // converts the rotationYaw of the Motion to integers to get keys

        float moveS = 0.0F; // is like the ClientSide moveStrafing moveForward
        float moveF = 0.0F;
        String key = "Nothing";

        int precision = String.valueOf((int) Math.abs(posX > posZ ? posX : posX)).length();
        precision = 15 - precision;
        double preD = 1.2 * Math.pow(10, -Math.max(3, precision - 5));

        if (Math.abs(Math.abs(mx) + Math.abs(mz)) > preD) {
            direction = (int) new BigDecimal(motionYaw).setScale(1, RoundingMode.HALF_UP).doubleValue();

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

        moveStrafing = moveS;
        moveForward = moveF;
        this.key = key;
    }

    private void calc(boolean checkCollisions) {
        flag = true;
        int precision = String.valueOf((int) Math.abs(posX > posZ ? posX : posX)).length();
        precision = 15 - precision - (isBelowSpecial ? 6 : 0);
        double preD = 1.2 * Math.pow(10, -Math.max(3, precision - 5));  // the motion deviates further and further from the coordinates 0 0 0. this value fix this

//		if (openInv) { // i don't have an Event for it
//			moveF = 0.0F;
//			moveS = 0.0F;
//			key = "NIX";
//		}

        // 1337 is an value to see that nothing's changed
        String diffString = "-1337";
        double diff = -1337;
        double closestdiff = 1337;

        int loops = 0; // how many tries the check needed to calculate the right motion (if i use for
        // loops)

        double flagJump = -1;
        found: for (int fastLoop = 2; fastLoop > 0; fastLoop--) { // if the Player changes the optifine fastmath
            // function
            fastMath = (fastLoop == 2) == fMath;
            for (int blockLoop = 2; blockLoop > 0; blockLoop--) { // if the Player blocks server side but not client
                // side (minecraft glitch)
                boolean blocking2 = (blockLoop == 1) != useSword;
                if (data.playerInfo.usingItem)
                    blocking2 = true;

                loops++;

                float moveStrafing = this.moveStrafing;
                float moveForward = this.moveForward;

                if (sneak) {
                    moveForward *= 0.3;
                    moveStrafing *= 0.3;
                }

//				if (openInv) {
//					if (sprint)
//						return;
//					if (sneak)
//						return;
//				}

                if (blocking2) { // if the player blocks with a sword
                    moveForward *= 0.2F;
                    moveStrafing *= 0.2F;
                }

                float jumpMovementFactor = 0.02F;
                if (lastSprint) {
                    jumpMovementFactor = 0.025999999F;
                }

                double var5;
                float var3 = 0.91f;

                if(lastOnGround) {
                    var3*= data.blockInfo.currentFriction;
                }

                aiMoveSpeed = data.getPlayer().getWalkSpeed() / 2D;
                if (sprint) {
                    //aiMoveSpeed/=0.76923071005;
                    aiMoveSpeed/=0.7692307779;
                }

                if(data.getPlayer().hasPotionEffect(PotionEffectType.SPEED)) {
                    aiMoveSpeed += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SPEED) * (0.20000000298023224D)) * aiMoveSpeed;
                }
                if(data.getPlayer().hasPotionEffect(PotionEffectType.SLOW)) {
                    aiMoveSpeed += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.SLOW) * (-0.15000000596046448D)) * aiMoveSpeed;
                }

                //Bukkit.broadcastMessage(aiMoveSpeed + ", " + Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer()));

                //aiMoveSpeed+= (data.getPlayer().getWalkSpeed() - 0.2) * 5 * 0.45;

                //Bukkit.broadcastMessage(aiMoveSpeed + "");

                float var4 = 0.16277136F / (var3 * var3 * var3);

                if (lastOnGround) {
                    var5 = aiMoveSpeed * var4;
                } else {
                    var5 = jumpMovementFactor;
                }

                double motionX = lmotionX, motionY = lmotionY, motionZ = lmotionZ;

                double var14 = moveStrafing * moveStrafing + moveForward * moveForward;
                if (var14 >= 1.0E-4F) {
                    var14 = sqrt_double(var14);
                    if (var14 < 1.0F)
                        var14 = 1.0F;
                    var14 = var5 / var14;
                    moveStrafing *= var14;
                    moveForward *= var14;

                    final float var15 = sin(yaw * (float) Math.PI / 180.0F); // cos, sin = Math function of optifine
                    final float var16 = cos(yaw * (float) Math.PI / 180.0F);
                    motionX += (moveStrafing * var16 - moveForward * var15);
                    motionZ += (moveForward * var16 + moveStrafing * var15);
                }

                /*if(checkCollisions) {
                    if(data.playerInfo.onLadder) {
                        float f6 = 0.15F;
                        motionX = MathHelper.clamp_double(motionX, -f6, f6);
                        motionZ = MathHelper.clamp_double(motionZ, -f6, f6);

                        motionY = Math.max(-0.15, motionY);

                        if (data.playerInfo.sneaking) motionY = Math.max(0, motionY);
                    }

                    double d3 = motionX;
                    double d4 = motionY;
                    double d5 = motionZ;

                    //moveEntity

                    SimpleCollisionBox box = data.box.copy();

                    List<SimpleCollisionBox> boxes = new ArrayList<>();

                    blockCollisions(data.blockInfo.handler.getBlocks(),
                            data.box.copy().addCoord(motionX, motionY, motionZ)).stream()
                            .map(block -> BlockData.getData(block.getType())
                                    .getBox(block, ProtocolVersion.getGameVersion()))
                            .forEach(blockBox -> blockBox.downCast(boxes));

                    //System.out.println("size=" + boxes.size());

                    double x = 0, y = 0, z = 0;
                    for (SimpleCollisionBox axisalignedbb1 : boxes) {
                        motionY = axisalignedbb1.copy().calculateYOffset(box, data.playerInfo.deltaY);
                    }

                    //box.offset(0.0D, y, 0.0D);

                    for (SimpleCollisionBox axisalignedbb2 : boxes) {
                        motionX = axisalignedbb2.copy().calculateXOffset(box, motionX);
                    }

                    //box.offset(x,0,0);

                    for (SimpleCollisionBox axisalignedbb13 : boxes) {
                        motionZ = axisalignedbb13.copy().calculateZOffset(box, motionZ);
                    }

                    //box.offset(0,0, z);

                    //Bukkit.broadcastMessage(x + ", " + y + ", " + z);

                    collidedHorizontally= d3 != motionX || d5 != motionZ;
                    collidedVertically = d4 != motionY;
                }*/


                predX = motionX;
                predZ = motionZ;

                final double diffX = rmotionX - motionX; // difference between the motion from the player and the
                // calculated motion
                final double diffZ = rmotionZ - motionZ;

                diff = Math.hypot(diffX, diffZ);

                if(Double.isNaN(diff) || Double.isInfinite(diff)) return;

                // if the motion isn't correct this value can get out in flags
                diff = new BigDecimal(diff).setScale(precision + 2, RoundingMode.HALF_UP).doubleValue();
                diffString = new BigDecimal(diff).setScale(precision + 2, RoundingMode.HALF_UP).toPlainString();

                if (diff < preD) { // if the diff is small enough
                    flag = false;
                    MiscUtils.testMessage(Color.Green + "(" + rmotionX + ", " + motionX + "); (" + rmotionZ + ", " + motionZ + ")");

                    MiscUtils.testMessage(Color.Green + diffString + " loops " + loops + " key: " + key + " sneak=" + sneak + " move=" + moveForward + " ai=" + aiMoveSpeed);
                    fMath = fastMath; // saves the fastmath option if the player changed it
                    break found;
                }
                MiscUtils.testMessage(Color.Red + "(" + rmotionX + ", " + motionX + "); (" + rmotionZ + ", " + motionZ + ")");
                MiscUtils.testMessage(Color.Red + diffString + " loops " + loops + " key: " + key + " sneak=" + sneak + " move=" + moveForward + " ai=" + aiMoveSpeed);

                if (diff < closestdiff) {
                    closestdiff = diff;
                }
            }
        }
    }

    public boolean checkConditions(final boolean lastTickSprint) {
        if (lPosX == 0 && lPosY == 0 && lPosZ == 0) { // the position is 0 when a moveFlying or look packet was send
            return false;
        }

        if (lastOnGround && !onGround && lastTickSprint) // if the Player jumps
            return false;

        if (rmotionX == 0 && rmotionZ == 0 && onGround)
            return false;

        if (MathUtils.hypot(lmotionX, lmotionZ) > 11) // if something gots wrong this can be helpfull
            return false;
        if (MathUtils.hypot(posX - lPosX, posZ - lPosZ) > 10)
            return false;

        return !fly
                && !data.playerInfo.creative;
    }

    private static final float[] SIN_TABLE_FAST = new float[4096];
    private static final float[] SIN_TABLE = new float[65536];

    public float sin(float p_76126_0_) {
        return fastMath ? SIN_TABLE_FAST[(int) (p_76126_0_ * 651.8986F) & 4095]
                : SIN_TABLE[(int) (p_76126_0_ * 10430.378F) & 65535];
    }

    public float cos(float p_76134_0_) {
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

    // functions of minecraft MathHelper.java
    public static float sqrt_float(float p_76129_0_) {
        return (float) Math.sqrt(p_76129_0_);
    }

    public static float sqrt_double(double p_76133_0_) {
        return (float) Math.sqrt(p_76133_0_);
    }

    private static List<Block> blockCollisions(List<Block> blocks, SimpleCollisionBox box) {
        return blocks.stream()
                .filter(b -> Helper.isCollided(box,
                        BlockData.getData(b.getType()).getBox(b, ProtocolVersion.getGameVersion())))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}