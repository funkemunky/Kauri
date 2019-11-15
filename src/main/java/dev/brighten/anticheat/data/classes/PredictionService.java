package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.*;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutAbilitiesPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.commands.KauriCommand;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MovementUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PredictionService {

    private ObjectData data;
    public boolean fly, velocity, position, sneak, sprint, useSword, hit, dropItem, inWeb, isCollidedHorizontally,
            lastOnGround, onGround, lastSprint, fMath, fastMath, walkSpecial, lastVelocity, isCollidedVertically, 
            isCollided;
    public double posX, posY, posZ, lPosX, lPosY, lPosZ, rmotionX, rmotionY, rmotionZ, lmotionX, lmotionZ, lmotionY;
    public double predX, predY, predZ;
    private double velocityX, velocityY, velocityZ;
    public TickTimer lastUseItem = new TickTimer(10);
    public BoundingBox box;
    public float walkSpeed, yaw, pitch, moveStrafing, moveForward, aiMoveSpeed, distanceWalkedModified,
            distanceWalkedOnStepModified, nextStepDistance, fallDistance;
    public String key = "Nothing";

    public PredictionService(ObjectData data) {
        this.data = data;
    }

    public void onSend(PacketSendEvent event) {
        switch(event.getType()) {
            case Packet.Server.POSITION: {
                TinyProtocolHandler.sendPacket(data.getPlayer(), new WrappedOutKeepAlivePacket(233).getObject());
                break;
            }
            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket packet = new WrappedOutVelocityPacket(event.getPacket(), event.getPlayer());

                if(packet.getId() == data.getPlayer().getEntityId()) {
                    velocityX = packet.getX();
                    velocityY = packet.getY();
                    velocityZ = packet.getZ();

                    dev.brighten.anticheat.utils.MiscUtils.testMessage("sent keepAlive");

                    TinyProtocolHandler.sendPacket(data.getPlayer(), new WrappedOutKeepAlivePacket(255).getObject());
                }
                break;
            }
            case Packet.Server.ABILITIES: {
                WrappedOutAbilitiesPacket packet = new WrappedOutAbilitiesPacket(event.getPacket(), event.getPlayer());

                if(packet.isAllowedFlight()) {
                    fly = true;
                } else fly = false;
                break;
            }
        }
    }

    public void onReceive(PacketReceiveEvent e) {
        switch(e.getType()) {
            case Packet.Client.KEEP_ALIVE: {
                WrappedInKeepAlivePacket packet = new WrappedInKeepAlivePacket(e.getPacket(), e.getPlayer());
                if(packet.getTime() == 255) {
                    velocity = true;
                    dev.brighten.anticheat.utils.MiscUtils
                            .testMessage("&evelX=" + velocityX + " velZ=" + velocityZ);
                } else if(packet.getTime() == 233) {
                    position = true;
                }
                break;
            }
            case Packet.Client.ABILITIES: {
                WrappedInAbilitiesPacket packet = new WrappedInAbilitiesPacket(e.getPacket(), e.getPlayer());

                if(packet.isAllowedFlight()) {
                    fly = true;
                } else {
                    fly = false;
                }

                walkSpeed = packet.getWalkSpeed();

                //Bukkit.broadcastMessage(packet.isAllowedFlight() + "");
                break;
            }
            case Packet.Client.ENTITY_ACTION: {
                WrappedInEntityActionPacket packet = new WrappedInEntityActionPacket(e.getPacket(), e.getPlayer());

                switch(packet.getAction()) {
                    case START_SNEAKING:
                        sneak = true;
                        break;
                    case STOP_SNEAKING:
                        sneak = false;
                        break;
                    case START_SPRINTING:
                        sprint = true;
                        break;
                    case STOP_SPRINTING:
                        sprint = false;
                        break;
                }

                //Bukkit.broadcastMessage("Action: " + packet.getAction().toString());
                break;
            }
            case Packet.Client.BLOCK_PLACE: {
                WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(e.getPacket(), e.getPlayer());

                if(packet.getItemStack() != null && packet.getPosition().getX() == -1 && packet.getPosition().getY() == -1 && packet.getPosition().getZ() == -1) {
                    if(packet.getItemStack().getType().toString().contains("SWORD")) {
                        useSword = true;
                        lastUseItem.reset();
                    }
                    //Bukkit.broadcastMessage(packet.getPosition().getX() + ", " + packet.getPosition().getY() + ", " + packet.getPosition().getZ());
                }
                break;
            }
            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket packet = new WrappedInUseEntityPacket(e.getPacket(), e.getPlayer());

                if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) && packet.getEntity() instanceof Player) {
                    hit = true;
                }
                break;
            }
            case Packet.Client.BLOCK_DIG: {
                WrappedInBlockDigPacket packet = new WrappedInBlockDigPacket(e.getPacket(), e.getPlayer());

                if(packet.getAction().equals(WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM)) {
                    useSword = false;
                } else if(packet.getAction().toString().contains("DROP")) {
                    dropItem = true;
                }
                break;
            }
            case Packet.Client.HELD_ITEM_SLOT: {
                useSword = false;
                break;
            }
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION:
            case Packet.Client.FLYING: {
                WrappedInFlyingPacket packet = new WrappedInFlyingPacket(e.getPacket(), e.getPlayer());
                
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

                dev.brighten.anticheat.utils.MiscUtils.testMessage(Color.Gray + rmotionX + ", " + rmotionZ);
                fMath = fastMath; // if the Player uses Optifine FastMath

                try {
                    if(!walkSpecial && !position && !velocity && !lastVelocity && checkConditions(lastSprint)) {
                        if (lastSprint && hit) { // If the Player Sprints and Hit a Player he get slowdown
                            lmotionX *= 0.6D;
                            lmotionZ *= 0.6D;
                        }
                        calc();
                    }

                    specialBlock = checkSpecialBlock(); // If the Player Walks on a Special block like Ice, Slime, Soulsand
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

                if(dropItem) {
                    useSword = false;
                }
                dropItem = false;

                double multiplier = 0.9100000262260437D; // multiplier = is the value that the client multiplies every move

                rmotionX *= multiplier;
                rmotionZ *= multiplier;

                if (lastOnGround) {
                    multiplier = 0.60000005239967D;
                    rmotionX *= multiplier;
                     rmotionZ *= multiplier;
                }

                if (Math.abs(rmotionX) < 0.005D) // the client sets the motionX,Y and Z to 0 if its slower than 0.005D
                    // because he would never stand still
                    rmotionX = 0.0D;
                if (Math.abs(rmotionY) < 0.005D)
                    rmotionY = 0.0D;
                if (Math.abs(rmotionZ) < 0.005D)
                    rmotionZ = 0.0D;

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
                break;
            }
        }
    }

    private boolean checkSpecialBlock() {
        return (data.playerInfo.iceTicks > 0 || data.playerInfo.wasOnSlime
                || data.playerInfo.soulSandTicks > 0 || data.playerInfo.climbTicks > 0) && (onGround || lastOnGround);
    }

    private void calc() {
        boolean flag = true;
        int precision = String.valueOf((int) Math.abs(posX > posZ ? posX : posX)).length();
        precision = 15 - precision;
        double preD = 1.2 * Math.pow(10, -Math.max(3, precision - 5));  // the motion deviates further and further from the coordinates 0 0 0. this value fix this

        double mx = rmotionX - lmotionX; // mx, mz is an Value to calculate the rotation and the Key of the Player
        double mz = rmotionZ - lmotionZ;

        float motionYaw = (float) (Math.atan2(mz, mx) * 180.0D / Math.PI) - 90.0F; // is the rotationYaw from the Motion
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

        moveF *= 0.98F;
        moveS *= 0.98F;

        moveStrafing = moveS;
        moveForward = moveF;
        this.key = key;

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

        double flagJumpp = -1;
        found: for (int fastLoop = 2; fastLoop > 0; fastLoop--) { // if the Player changes the optifine fastmath
            // function
            fastMath = fastLoop == 2 ? fMath : !fMath;
            for (int blockLoop = 2; blockLoop > 0; blockLoop--) { // if the Player blocks server side but not client
                // side (minecraft glitch)
                boolean blocking2 = blockLoop == 1 ? !useSword : useSword;
                if (data.playerInfo.usingItem)
                    blocking2 = true;

                loops++;

                float moveStrafing = moveS;
                float moveForward = moveF;

                if (sneak) {
                    if (sprint)
                        return;
                    moveForward *= 0.3F;
                    moveStrafing *= 0.3F;
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

                float var5;
                float var3 = MovementUtils.getFriction(data) * 0.91f;

                aiMoveSpeed =  0.1f;
                if (sprint)
                    aiMoveSpeed += 0.03000001F;

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

                double motionX = lmotionX;
                double motionZ = lmotionZ;

                float var14 = moveStrafing * moveStrafing + moveForward * moveForward;
                if (var14 >= 1.0E-4F) {
                    var14 = sqrt_float(var14);
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

                predX = motionX;
                predZ = motionZ;

                final double diffX = rmotionX - motionX; // difference between the motion from the player and the
                // calculated motion
                final double diffZ = rmotionZ - motionZ;

                diff = Math.hypot(diffX, diffZ);

                // if the motion isn't correct this value can get out in flags
                diff = new BigDecimal(diff).setScale(precision + 2, RoundingMode.HALF_UP).doubleValue();
                diffString = new BigDecimal(diff).setScale(precision + 2, RoundingMode.HALF_UP).toPlainString();

                if (diff < preD) { // if the diff is small enough
                    //Bukkit.broadcastMessage(diffString + " loops " + loops + " key: " + key);
                    flag = false;
                    //Bukkit.broadcastMessage(Color.Green + "(" + rmotionX + ", " + motionX + "); (" + rmotionZ + ", " + motionZ + ")");

                    fMath = fastMath; // saves the fastmath option if the player changed it
                    break found;
                } else {
                   // Bukkit.broadcastMessage(Color.Red + "(" + rmotionX + ", " + motionX + "); (" + rmotionZ + ", " + motionZ + ")");
                }

                if (diff < closestdiff) {
                    closestdiff = diff;
                }
            }
        }

        if(flag) {
           // Bukkit.broadcastMessage(Color.Red + diffString + " loops " + loops + " key: " + key + " sneak: " + sneak + " jump: " + flagJumpp + " onground: " + onGround + ", " + data.playerInfo.serverGround);
        }
       // Bukkit.broadcastMessage(data.playerInfo.deltaXZ + "");
    }

    private void moveEntity(double x, double y, double z) {
        if(!data.playerInfo.worldLoaded) return;
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;

        if (inWeb) {
            inWeb = false;
            x *= 0.25D;
            y *= 0.05000000074505806D;
            z *= 0.25D;
            this.rmotionX = 0.0D;
            this.rmotionY = 0.0D;
            this.rmotionZ = 0.0D;
        }

        double d3 = x;
        double d4 = y;
        double d5 = z;
        boolean flag = this.onGround && data.playerInfo.sneaking;


        if (flag) {
            double d6;

            for (d6 = 0.05D; x != 0.0D && getBoxes(box.add((float)x, -1.0f, 0.0f)).isEmpty(); d3 = x) {
                if (x < d6 && x >= -d6) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= d6;
                } else {
                    x += d6;
                }
            }

            for (; z != 0.0D && getBoxes(box.add(0.0f, -1.0f, (float)z)).isEmpty(); d5 = z) {
                if (z < d6 && z >= -d6) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= d6;
                } else {
                    z += d6;
                }
            }

            for (; x != 0.0D && z != 0.0D && getBoxes(box.add((float)x, -1.0f, (float)z)).isEmpty(); d5 = z) {
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

        List<BoundingBox> list1 = getBoxes(box.addCoord((float)x, (float)y, (float)z));

        for (BoundingBox axisalignedbb1 : list1) {
            y = axisalignedbb1.calculateYOffset(box, y);
        }

        box = box.add(0, (float)y, 0);
        boolean flag1 = this.onGround || d4 != y && d4 < 0.0D;

        for (BoundingBox axisalignedbb2 : list1) {
            x = axisalignedbb2.calculateXOffset(box, x);
        }

        box = box.add((float)x, 0, 0);

        for (BoundingBox axisalignedbb13 : list1) {
            z = axisalignedbb13.calculateZOffset(box, z);
        }

        box = box.add(0, 0, (float)z);

        this.resetPositionToBB();
        this.isCollidedHorizontally = d3 != x || d5 != z;
        this.isCollidedVertically = d4 != y;
        this.onGround = this.isCollidedVertically && d4 < 0.0D;
        this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
        int k = MathHelper.floor_double(this.posZ);
        Location blockpos = new Location(data.getPlayer().getWorld(), i, j, k);
        Block block1 = blockpos.getBlock();

        if (block1.getType().toString().contains("AIR")) {
            Block block = block1.getRelative(BlockFace.DOWN);

            if(BlockUtils.isFence(block)) {
                block1 = block;
                blockpos = block.getLocation();
            }
        }

        this.updateFallState(y, this.onGround, block1, blockpos);

        if (d3 != x) {
            this.rmotionX = 0.0D;
        }

        if (d5 != z) {
            this.rmotionZ = 0.0D;
        }

        //Onlanded function
        if (d4 != y) {
            if(!data.playerInfo.sneaking && rmotionY < 0 && block1.getType().toString().contains("SLIME")) {
                rmotionY = -rmotionY;
            }
        }

        if (!flag && data.getPlayer().getVehicle() == null) {
            double d12 = this.posX - d0;
            double d13 = this.posY - d1;
            double d14 = this.posZ - d2;

            if (!BlockUtils.isClimbableBlock(block1)) {
                d13 = 0.0D;
            }

            //Collide with block function
            if (block1 != null && this.onGround) {
                onCollideWithBlock(block1);
            }

            this.distanceWalkedModified = (float) ((double) this.distanceWalkedModified + (double) MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6D);
            this.distanceWalkedOnStepModified = (float) ((double) this.distanceWalkedOnStepModified + (double) MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);

            if (this.distanceWalkedOnStepModified > this.nextStepDistance && !block1.getType().toString().contains("AIR")) {
                this.nextStepDistance = (int) this.distanceWalkedOnStepModified + 1;
            }
        }

        this.doBlockCollisions();
    }

    private void resetPositionToBB() {
        this.posX = (box.minX + box.maxX) / 2.0D;
        this.posY = box.minY;
        this.posZ = (box.minZ + box.maxZ) / 2.0D;
    }
    
    private void onCollideWithBlock(Block block1) {
        String material = block1.getType().toString();
        
        if(material.contains("SLIME")) {
            if (!data.playerInfo.sneaking && Math.abs(rmotionY) < 0.1D)
            {
                double d0 = 0.4D + Math.abs(rmotionY) * 0.2D;
                rmotionX *= d0;
                rmotionZ *= d0;
            }
        }
    }

    private void doBlockCollisions() {
        Location blockpos = new Location(data.getPlayer().getWorld(), box.minX + 0.001D, box.minY + 0.001D, box.minZ + 0.001D);
        Location blockpos1 = new Location(data.getPlayer().getWorld(), box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D);

        if (data.playerInfo.worldLoaded) {
            for (int i = blockpos.getBlockX(); i <= blockpos1.getBlockX(); ++i) {
                for (int j = blockpos.getBlockY(); j <= blockpos1.getBlockY(); ++j) {
                    for (int k = blockpos.getBlockZ(); k <= blockpos1.getBlockZ(); ++k) {
                        Block block = new Location(data.getPlayer().getWorld(), i, j, k).getBlock();

                        onCollideWithBlock(block);
                    }
                }
            }
        }
    }
    
    private List<BoundingBox> getBoxes(BoundingBox box) {
        return Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(data.getPlayer().getWorld(), box);
    }

    private boolean isSneakingLikeWallDurh() {
        double roundedX = Math.abs(MathUtils.round(posX, 1, RoundingMode.HALF_UP)), roundedZ = Math.abs(MathUtils.round(posZ, 1, RoundingMode.HALF_UP));
        return sneak && (MathUtils.approxEquals(5E-3, roundedX % 1, 0.3) || MathUtils.approxEquals(5E-3, roundedX % 1, 0.7)|| MathUtils.approxEquals(5E-3, roundedZ % 1, 0.3) || MathUtils.approxEquals(5E-3, roundedZ % 1, 0.7));
    }

    boolean checkConditions(final boolean lastTickSprint) {
        if (lPosX == 0 && lPosY == 0 && lPosZ == 0) { // the position is 0 when a moveFlying or look packet was send
            return false;
        }

        if (lastOnGround && !onGround) // if the Player jumps
            return false;

        if (rmotionX == 0 && rmotionZ == 0 && onGround)
            return false;

        if (Math.hypot(lmotionX, lmotionZ) > 11) // if something gots wrong this can be helpfull
            return false;
        if (Math.hypot(posX - lPosX, posZ - lPosZ) > 10)
            return false;

        if (data.playerInfo.liquidTicks > 0
                || data.playerInfo.climbTicks > 0
                || fly
                || data.getPlayer().getGameMode().toString().contains("SPEC")) {
            return false;
        }

        return true;
    }

    private void updateFallState(double y, boolean onGroundIn, Block blockIn, Location pos) {
        if (onGroundIn) {
            if (this.fallDistance > 0.0F)
                this.fallDistance = 0.0F;
        } else if (y < 0.0D) {
            this.fallDistance = (float) ((double) this.fallDistance - y);
        }
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
            SIN_TABLE_FAST[i] = (float) Math.sin((double) (((float) i + 0.5F) / 4096.0F * ((float) Math.PI * 2F)));
        }

        for (i = 0; i < 360; i += 90) {
            SIN_TABLE_FAST[(int) ((float) i * 11.377778F) & 4095] = (float) Math
                    .sin((double) ((float) i * 0.017453292F));
        }
    }

    // functions of minecraft MathHelper.java
    public static float sqrt_float(float p_76129_0_) {
        return (float) Math.sqrt((double) p_76129_0_);
    }

    public static float sqrt_double(double p_76133_0_) {
        return (float) Math.sqrt(p_76133_0_);
    }
}
