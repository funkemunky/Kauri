package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PredictionService {

    private ObjectData data;
    public double motionX, motionY, motionZ;
    public boolean isAirBorne, lastOnGround;
    public float moveStrafing, moveForward, aiMoveSpeed, distanceWalkedModified, distanceWalkedOnStepModified, fallDistance;
    public double posX, posY, posZ, lPosX, lPosY, lPosZ, nextStepDistance;
    public String key;

    public PredictionService(ObjectData data) {
        this.data = data;
    }

    public void pre(WrappedInFlyingPacket packet) {
        if(data.playerInfo.to == null) return;
        if (Math.abs(this.motionX) < 0.005D) {
            this.motionX = 0.0D;
        }

        if (Math.abs(this.motionY) < 0.005D) {
            this.motionY = 0.0D;
        }

        if (Math.abs(this.motionZ) < 0.005D) {
            this.motionZ = 0.0D;
        }

        int precision = String.valueOf((int) Math.abs(data.playerInfo.to.x > data.playerInfo.to.z ? data.playerInfo.to.x : data.playerInfo.to.x)).length();
        precision = 15 - precision;
        double preD = Double.parseDouble("1.2E-" + Math.max(3, precision - 5)); // the motion deviates further and further from the coordinates 0 0 0. this value fix this

        double mx = data.playerInfo.deltaX - data.playerInfo.lDeltaX * 0.91f; // mx, mz is an Value to calculate the rotation and the Key of the Player
        double mz = data.playerInfo.deltaZ - data.playerInfo.lDeltaZ * 0.91f;

        float motionYaw = (float) (Math.atan2(mz, mx) * 180.0D / Math.PI) - 90.0F;

        int direction = 6;

        motionYaw -= data.playerInfo.to.yaw;

        while (motionYaw > 360.0F)
            motionYaw -= 360.0F;
        while (motionYaw < 0.0F)
            motionYaw += 360.0F;

        motionYaw /= 45.0F; // converts the rotationYaw of the Motion to integers to get keys

        float moveS = 0.0F; // is like the ClientSide moveStrafing moveForward
        float moveF = 0.0F;
        String key = "Nothing";

        if(Math.abs(Math.abs(mx) + Math.abs(mz)) > preD) {
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

        this.key = key;
        moveStrafing = moveS;
        moveForward = moveF;

        moveStrafing*= 0.98f;
        moveForward*= 0.98f;

        //Setting AI move speed
        aiMoveSpeed = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer());
    }

    public void move(WrappedInFlyingPacket packet) {
        if(data.playerInfo.to == null) return;
        if(lastOnGround && !data.playerInfo.collidedGround && !isAirBorne && data.playerInfo.deltaY > 0) {
            jump();
        }
        moveEntityWithHeading(moveStrafing, moveForward);
    }

    public void velocity(WrappedOutVelocityPacket packet) {

    }

    public void useEntity(WrappedInUseEntityPacket packet) {

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

    private void moveEntityWithHeading(float strafe, float forward) {
        if (!data.blockInfo.inWater || data.playerInfo.isFlying) {
            if (!data.blockInfo.inLava || data.playerInfo.isFlying) {
                float f4 = 0.91F;

                Location loc = new Location(data.getPlayer().getWorld(), posX, data.box.minY, posZ);
                if (data.playerInfo.collidedGround) {
                    Block block = BlockUtils.getBlock(loc.clone().subtract(0, 1,0));
                    f4 = block != null ? ReflectionsUtil.getFriction(block) : 0.6f * 0.91f;
                }

                Block block = BlockUtils.getBlock(loc);
                boolean onLadder = block != null ? BlockUtils.isClimbableBlock(block) : false;

                float f = 0.16277136F / (f4 * f4 * f4);
                float f5;

                if (data.playerInfo.collidedGround) {
                    f5 = aiMoveSpeed * f;
                } else {
                    f5 = data.playerInfo.sprinting ? 0.026f : 0.02f;
                }

                this.moveFlying(strafe, forward, f5);

                if (onLadder) {
                    float f6 = 0.15F;
                    this.motionX = MathHelper.clamp_double(this.motionX, (double) (-f6), (double) f6);
                    this.motionZ = MathHelper.clamp_double(this.motionZ, (double) (-f6), (double) f6);
                    this.fallDistance = 0.0F;

                    if (this.motionY < -0.15D) {
                        this.motionY = -0.15D;
                    }

                    boolean flag = data.playerInfo.sneaking;

                    if (flag && this.motionY < 0.0D) {
                        this.motionY = 0.0D;
                    }
                }

                this.moveEntity(this.motionX, this.motionY, this.motionZ);

                if (data.playerInfo.collidesHorizontally && onLadder) {
                    this.motionY = 0.2D;
                }

                if (!Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(loc)) {
                    if (this.posY > 0.0D) {
                        this.motionY = -0.1D;
                    } else {
                        this.motionY = 0.0D;
                    }
                } else {
                    this.motionY -= 0.08D;
                }

                this.motionY *= 0.9800000190734863D;
                this.motionX *= f4;
                this.motionZ *= f4;
            } else {
                double d1 = this.posY;
                this.moveFlying(strafe, forward, 0.02F);
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.5D;
                this.motionY *= 0.5D;
                this.motionZ *= 0.5D;
                this.motionY -= 0.02D;

                if (data.playerInfo.collidesHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d1, this.motionZ)) {
                    this.motionY = 0.30000001192092896D;
                }
            }
        } else {
            double d0 = this.posY;
            float f1 = 0.8F;
            float f2 = 0.02F;
            float f3 = (float) PlayerUtils.getDepthStriderLevel(data.getPlayer());

            if (f3 > 3.0F) {
                f3 = 3.0F;
            }

            if (!data.playerInfo.collidedGround) {
                f3 *= 0.5F;
            }

            if (f3 > 0.0F) {
                f1 += (0.54600006F - f1) * f3 / 3.0F;
                f2 += (aiMoveSpeed * 1.0F - f2) * f3 / 3.0F;
            }

            this.moveFlying(strafe, forward, f2);
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double) f1;
            this.motionY *= 0.800000011920929D;
            this.motionZ *= (double) f1;
            this.motionY -= 0.02D;

            if (data.playerInfo.collidesHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6000000238418579D - this.posY + d0, this.motionZ)) {
                this.motionY = 0.30000001192092896D;
            }
        }
    }

    private void moveEntity(double x, double y, double z) {
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;

        if (data.blockInfo.inWeb) {
            x *= 0.25D;
            y *= 0.05000000074505806D;
            z *= 0.25D;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }

        double d3 = x;
        double d4 = y;
        double d5 = z;
        boolean flag = data.playerInfo.collidedGround && data.playerInfo.sneaking;


        if (flag) {
            double d6;

            for (d6 = 0.05D; x != 0.0D && Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .getCollidingBoxes(data.getPlayer().getWorld(), data.box.add((float)x, -1.0f, 0.0f)).isEmpty(); d3 = x) {
                if (x < d6 && x >= -d6) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= d6;
                } else {
                    x += d6;
                }
            }

            for (; z != 0.0D && Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .getCollidingBoxes(data.getPlayer().getWorld(), data.box.add(0.0f, -1.0f, (float)z)).isEmpty(); d5 = z) {
                if (z < d6 && z >= -d6) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= d6;
                } else {
                    z += d6;
                }
            }

            for (; x != 0.0D && z != 0.0D && Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .getCollidingBoxes(data.getPlayer().getWorld(), data.box.add((float)x, -1.0f, (float)z)).isEmpty(); d5 = z) {
                if (x < d6 && x >= -d6) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= d6;
                } else {
                    x += d6;
                }

                d3 = x;

                if (z < d6 && z >= -d6) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= d6;
                } else {
                    z += d6;
                }
            }
        }

        List<BoundingBox> list1 = Atlas.getInstance().getBlockBoxManager().getBlockBox()
                .getCollidingBoxes(data.getPlayer().getWorld(), data.box.addCoord((float)x, (float)y, (float)z));
        BoundingBox boundingBox = data.box;

        for (BoundingBox boundingBox1 : list1) {
            y = boundingBox1.calculateYOffset(boundingBox, y);
        }

        boundingBox = boundingBox.add(0.0F, (float)y, 0.0F);
        boolean flag1 = data.playerInfo.collidedGround || d4 != y && d4 < 0.0F;

        for (BoundingBox boundingBox2 : list1) {
            x = boundingBox2.calculateXOffset(boundingBox, x);
        }

        boundingBox = boundingBox.add((float)x, 0.0F, 0.0F);

        for (BoundingBox boundingBox13 : list1) {
            z = boundingBox13.calculateZOffset(boundingBox, z);
        }

        boundingBox = boundingBox.add(0.0F, 0.0F, (float)z);
        
        this.resetPositionToBB();
        data.playerInfo.collidesHorizontally = d3 != x || d5 != z;
        data.playerInfo.collidesVertically = d4 != y;
        lastOnGround = data.playerInfo.collidedGround;
        data.playerInfo.collidedGround = data.playerInfo.collidesVertically && d4 < 0.0D;
        data.playerInfo.collided = data.playerInfo.collidesHorizontally || data.playerInfo.collidesVertically;
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
        int k = MathHelper.floor_double(this.posZ);
        Location blockpos = new Location(data.getPlayer().getWorld(), i, j, k);
        Block block1 = BlockUtils.getBlock(blockpos);

        if (block1.getType().equals(Material.AIR)) {
            Block block = BlockUtils.getBlock(blockpos.clone().subtract(0, 1, 0));

            if(BlockUtils.isFence(block) || BlockUtils.isFenceGate(block) || block.getType().toString().contains("WALL")) {
                block1 = block;
                blockpos.subtract(0,1,0);
            }
        }

        this.updateFallState(y, data.playerInfo.collidedGround, block1, blockpos);

        if (d3 != x) {
            this.motionX = 0.0D;
        }

        if (d5 != z) {
            this.motionZ = 0.0D;
        }

        //Landing stuff
        if (d4 != y) {
            if(block1.getType().toString().contains("SLIME") && !data.playerInfo.sneaking && motionY < 0) {
                motionY = -motionY;
            } else motionY = 0;
        }

        if (!flag && data.getPlayer().getVehicle() == null) {
            double d12 = this.posX - d0;
            double d13 = this.posY - d1;
            double d14 = this.posZ - d2;

            if (!BlockUtils.isClimbableBlock(block1)) {
                d13 = 0.0D;
            }

            this.distanceWalkedModified = (float) ((double) this.distanceWalkedModified + (double) MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
            this.distanceWalkedOnStepModified = (float) ((double) this.distanceWalkedOnStepModified + (double) MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

            if (this.distanceWalkedOnStepModified > (float) this.nextStepDistance && !block1.getType().equals(Material.AIR)) {
                this.nextStepDistance = (int) this.distanceWalkedOnStepModified + 1;

                if (data.blockInfo.inWater) {
                    float f = MathHelper.sqrt_double(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;

                    if (f > 1.0F) {
                        f = 1.0F;
                    }
                }
            }
        }

        /*try {
            this.doBlockCollisions();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
            this.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }*/

       /* boolean flag2 = this.isWet();

        if (this.worldObj.isFlammableWithin(boundingBox.contract(0.001D, 0.001D, 0.001D))) {
            this.dealFireDamage(1);

            if (!flag2) {
                ++this.fire;

                if (this.fire == 0) {
                    this.setFire(8);
                }
            }
        } else if (this.fire <= 0) {
            this.fire = -this.fireResistance;
        }*/
    }


    /**
     * Checks if the offset position from the entity's current position is inside of liquid. Args: x, y, z
     */
    private boolean isOffsetPositionInLiquid(double x, double y, double z) {
        BoundingBox box = data.box.add((float)x, (float)y, (float)z);
        return data.blockInfo.allBlocks.stream().filter(entry -> entry.getValue().collides(box)).anyMatch(entry -> BlockUtils.isLiquid(entry.getKey()));
    }

    private void resetPositionToBB() {
        this.posX = (data.box.minX + data.box.maxX) / 2.0D;
        this.posY = data.box.minY;
        this.posZ = (data.box.minZ + data.box.maxZ) / 2.0D;
    }

    private void updateFallState(double y, boolean onGroundIn, Block blockIn, Location pos) {
        if (onGroundIn) {
            if (this.fallDistance > 0.0F) {
                if (blockIn != null 
                        && blockIn.getType().toString().contains("SLIME") 
                        && !data.playerInfo.sneaking) {
                    this.fall(this.fallDistance, 0F);
                } else {
                    this.fall(this.fallDistance, 1.0F);
                }

                this.fallDistance = 0.0F;
            }
        } else if (y < 0.0D) {
            this.fallDistance = (float) ((double) this.fallDistance - y);
        }
    }

    private void fall(float distance, float damageMultiplier) {
        float f = (float) PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP);
        int i = MathHelper.ceiling_float_int((distance - 3.0F - f) * damageMultiplier);

        if (i > 0) {
            //TODO Apply fall damage to player.
            //this.attackEntityFrom(DamageSource.fall, (float) i);
            int j = MathHelper.floor_double(this.posX);
            int k = MathHelper.floor_double(this.posY - 0.20000000298023224D);
            int l = MathHelper.floor_double(this.posZ);
        }
    }

    private void jump() {
        this.motionY = 0.42;

        if (data.getPlayer().hasPotionEffect(PotionEffectType.JUMP)) {
            this.motionY += (double) (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP) * 0.1F);
        }

        if (data.playerInfo.sprinting) {
            float f = data.playerInfo.to.yaw * 0.017453292F;
            this.motionX -= (MathHelper.sin(f) * 0.2F);
            this.motionZ += (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
    }
}
