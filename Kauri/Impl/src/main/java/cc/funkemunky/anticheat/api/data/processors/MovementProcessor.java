package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.CollisionAssessment;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.PastLocation;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.entity.Vehicle;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MovementProcessor {
    private boolean lastFlight, flight, clientOnGround, serverOnGround, fullyInAir, inAir, hasJumped, inLiquid, blocksOnTop, pistonsNear, onHalfBlock,
            onClimbable, onIce, collidesHorizontally, tookVelocity, inWeb, onSlime, onSlimeBefore, onSoulSand, isRiptiding, halfBlocksAround, isNearGround, isInsideBlock, blocksNear, blocksAround;
    private int airTicks, groundTicks, iceTicks, climbTicks, halfBlockTicks, soulSandTicks, blockAboveTicks, optifineTicks, liquidTicks, webTicks, yawZeroTicks, pitchZeroTicks;
    private float deltaY, lastDeltaXZ, slimeHeight, fallDistance, yawDelta, pitchDelta, lastYawDelta, lastPitchDelta, lastDeltaY, deltaXZ, lastServerYVelocity, serverYAcceleration, clientYAcceleration, lastClientYAcceleration, lastServerYAcceleration, jumpVelocity, cinematicYawDelta, cinematicPitchDelta, lastCinematicPitchDelta, lastCinematicYawDelta;
    private CustomLocation from, to;
    private double motX, motY, motZ, lastMotX, lastMotY, lastMotZ;
    @Setter
    private float serverYVelocity;
    private PastLocation pastLocation = new PastLocation();
    private TickTimer lastRiptide = new TickTimer(6), lastVehicle = new TickTimer(4), lastFlightToggle = new TickTimer(10);
    private List<BoundingBox> boxes = new ArrayList<>();
    private long lastTimeStamp, lookTicks, lagTicks;

    public void update(PlayerData data, WrappedInFlyingPacket packet) {
        val player = packet.getPlayer();
        val timeStamp = System.currentTimeMillis();
        boolean chunkLoaded = Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(player.getLocation());
        if (from == null || to == null) {
            from = new CustomLocation(0, 0, 0, 0, 0);
            to = new CustomLocation(0, 0, 0, 0, 0);
        }


       // predict(data, .98f, .98f, true);
        from = to.clone();
        clientOnGround = packet.isGround();

        if (packet.isPos()) {
            to.setX(packet.getX());
            to.setY(packet.getY());
            to.setZ(packet.getZ());

            data.setBoundingBox(new BoundingBox(to.toVector(), to.toVector()).grow(0.3f, 0, 0.3f).add(0,0,0,0,1.84f,0));

            if (chunkLoaded) {
                //Here we get the colliding boundingboxes surrounding the player.
                List<BoundingBox> box = boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(player.getWorld(), data.getBoundingBox().grow(2f, 2f, 2f));

                CollisionAssessment assessment = new CollisionAssessment(data.getBoundingBox(), data);

                //There are some entities that are collide-able like boats but are not considered blocks.
                player.getNearbyEntities(1, 1, 1).stream().filter(entity -> entity instanceof Vehicle || entity.getType().name().toLowerCase().contains("shulker")).forEach(entity -> assessment.assessBox(ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(entity)), player.getWorld(), true));

                //Now we scrub through the colliding boxes for any important information that could be fed into detections.
                box.forEach(bb -> assessment.assessBox(bb, player.getWorld(), false));


                serverOnGround = assessment.isOnGround();
                blocksOnTop = assessment.isBlocksOnTop();
                collidesHorizontally = assessment.isCollidesHorizontally();
                inLiquid = assessment.isInLiquid();
                onHalfBlock = assessment.isOnHalfBlock();
                onIce = assessment.isOnIce();
                pistonsNear = assessment.isPistonsNear();
                inWeb = assessment.isInWeb();
                onClimbable = assessment.isOnClimbable();
                fullyInAir = assessment.isFullyInAir();
                onSoulSand = assessment.isOnSoulSand();
                blocksAround = assessment.isBlocksAround();
                blocksNear = assessment.isBlocksNear();
                halfBlocksAround = assessment.getMaterialsCollided().stream().anyMatch(material -> material.toString().contains("STAIR") || material.toString().contains("STEP") || material.toString().contains("SLAB") || material.toString().contains("SNOW") || material.toString().contains("CAKE") || material.toString().contains("BED") || material.toString().contains("SKULL"));

                isNearGround = isNearGround(data, 1.5f);
                onSlime = assessment.isOnSlime();

                if (serverOnGround) {
                    groundTicks++;
                    airTicks = 0;

                    onSlimeBefore = onSlime;
                } else {
                    airTicks++;
                    groundTicks = 0;
                }

                if(onSlime) {
                    slimeHeight = fallDistance * 0.75f;
                }
            }
            tookVelocity = System.currentTimeMillis() - data.getVelocityProcessor().getLastVelocityTimestamp() < 1500L;
            jumpVelocity = 0.42f + (PlayerUtils.getPotionEffectLevel(packet.getPlayer(), PotionEffectType.JUMP) * 0.1f);
            val vecStream = data.getTeleportLocations().stream().filter(vec -> vec.distance(to.toVector()) < 0.45).findFirst().orElse(null);

            if(vecStream != null) {
                data.setLastServerPosStamp(System.currentTimeMillis());
                data.getTeleportLocations().remove(vecStream);
                from = to;
            }


            lastDeltaY = deltaY;
            lastDeltaXZ = deltaXZ;
            deltaY = (float) (to.getY() - from.getY());
            deltaXZ = (float) (MiscUtils.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()));
            lastClientYAcceleration = clientYAcceleration;
            clientYAcceleration = deltaY - lastDeltaY;

            //Hear we use the client's ground packet being sent since whatever motion the client says it has
            //will line up with this since ground is sent along with positional packets (flying, poslook, pos, look)
            if (hasJumped) {
                hasJumped = false;
                inAir = true;
            } else if (clientOnGround) {
                inAir = false;
            } else if (!inAir) {
                hasJumped = true;
                //bF(data);
            }

            if(serverOnGround) {
                fallDistance = 0;
            } else fallDistance = Math.max(0, fallDistance - deltaY);

            lastFlight = flight;
            flight = player.getAllowFlight();
            if (flight != lastFlight) {
                getLastFlightToggle().reset();
            }


            if(timeStamp - lastTimeStamp > 100) {
                lagTicks = (timeStamp - lastTimeStamp - 50) / 50;
            } else lagTicks-= lagTicks > 0 ? 1 : 0;

            if(lagTicks > 0) {
                data.getLastLag().reset();
            }
            val block = BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()));
            val blockAbove = BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0));
            isInsideBlock = block == null || blockAbove == null || BlockUtils.isSolid(block) || BlockUtils.isSolid(blockAbove);

            if (isRiptiding = Atlas.getInstance().getBlockBoxManager().getBlockBox().isRiptiding(packet.getPlayer()))
                lastRiptide.reset();
            
            lastServerYVelocity = serverYVelocity;

            if (hasJumped) {
                serverYVelocity = Math.min(deltaY, 0.42f);
            } else if (inAir) {
                serverYVelocity = (serverYVelocity - 0.08f) * 0.98f;

                if(Math.abs(serverYVelocity) < 0.005) {
                    serverYVelocity = 0;
                }

                if(deltaXZ == 0 & serverYVelocity == 0) {
                    serverYVelocity = (serverYVelocity - 0.08f) * 0.98f;
                }
            } else {
                serverYVelocity = 0;
            }

            if(timeStamp - data.getVelocityProcessor().getLastVelocityTimestamp() <= 100L) {
                serverYVelocity = data.getVelocityProcessor().getMotionY();
            }

            if (getLastFlightToggle().hasNotPassed(3)) {
                serverYVelocity = deltaY;
            }

            if(System.currentTimeMillis() - data.getLastServerPosStamp() < 100L) {
                serverYVelocity = deltaY;
            }

            lastServerYAcceleration = serverYAcceleration;
            serverYAcceleration = serverYVelocity - lastServerYVelocity;

            //The MiscUtils#getDistanceToGround method is kind of heavy, so we only run it 4 times a second instead of 20.
            //We compensate for the loss of data by using the yDelta of the player to guess the distance.
            //This method should and won't be used for anything sensitive requiring precise data.
            //This is just used for preventing any false positives

            lastTimeStamp = timeStamp;

            if(data.isLoggedIn() && data.getLastLogin().hasPassed(2)) data.setLoggedIn(false);


            iceTicks = onIce ? Math.min(40, iceTicks + 1) : Math.max(0, iceTicks - 1);
            climbTicks = onClimbable ? Math.min(40, climbTicks + 1) : Math.max(0, climbTicks - 1);
            halfBlockTicks = onHalfBlock ? Math.min(40, halfBlockTicks + 2) : Math.max(0, halfBlockTicks - 1);
            blockAboveTicks = blocksOnTop ? Math.min(40, blockAboveTicks + 2) : Math.max(0, blockAboveTicks - 1);
            liquidTicks = inLiquid ? Math.min(50, liquidTicks + 1) : Math.max(0, liquidTicks - 1);
            soulSandTicks = onSoulSand ? Math.min(40, soulSandTicks + 1) : Math.max(0, soulSandTicks - 1);
            webTicks = inWeb ? Math.min(30, webTicks + 1) : Math.max(webTicks, webTicks - 1);
        } else if(!packet.isLook() && data.isLoggedIn()) {
            data.getLastLogin().reset();
        }
        if (player.getVehicle() != null || PlayerUtils.isGliding(player)) lastVehicle.reset();

        if (packet.isLook()) {
            to.setYaw(packet.getYaw());
            to.setPitch(packet.getPitch());

            //Algorithm stripped from the MC client which calculates the deceleration of rotation when using cinematic/optifine zoom.
            //Used to separate a legitimate aimbot-like rotation from a cheat.
            lastYawDelta = yawDelta;
            lastPitchDelta = pitchDelta;
            float yawDelta = this.yawDelta = MathUtils.getDelta(to.getYaw(), from.getYaw()), pitchDelta = this.pitchDelta = MathUtils.getDelta(to.getPitch(), from.getPitch());
            float smooth = data.getYawSmooth().smooth(yawDelta, MiscUtils.convertToMouseDelta(yawDelta)), smooth2 = data.getPitchSmooth().smooth(pitchDelta, MiscUtils.convertToMouseDelta(pitchDelta));
            if(data.isLoggedIn()) data.setLoggedIn(false);
            val smoothDelta = MathUtils.getDelta(yawDelta, smooth);
            val smoothDelta2 = MathUtils.getDelta(pitchDelta, smooth2);

            data.setCinematicMode((smoothDelta / yawDelta) < 0.1 || (smoothDelta2) < 0.02);

            lookTicks++;

            //Bukkit.broadcastMessage(smoothDelta + "," + smoothDelta2 + ": " + "(" + smoothDelta / yawDelta + "), " + "(" + (smoothDelta2 / pitchDelta) + "): " + data.isCinematicMode());
            if (data.isCinematicMode()) {
                optifineTicks += optifineTicks < 60 ? 1 : 0;
            } else if (optifineTicks > 0) {
                optifineTicks -= 3;
            }
            lastCinematicYawDelta = cinematicYawDelta;
            lastCinematicPitchDelta = cinematicPitchDelta;
        }

        if (to.getYaw() == from.getYaw()) {
            yawZeroTicks = Math.min(20, yawZeroTicks + 1);
        } else yawZeroTicks -= yawZeroTicks > 0 ? 1 : 0;

        if (to.getPitch() == from.getPitch()) {
            pitchZeroTicks = Math.min(20, pitchZeroTicks + 1);
        } else pitchZeroTicks -= pitchZeroTicks > 0 ? 1 : 0;

        lastMotX = motX;
        lastMotY = motY;
        lastMotZ = motZ;
        //predict(data, .98f, .98f, false);

        pastLocation.addLocation(new CustomLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
        data.setGeneralCancel(data.isServerPos() || data.isLagging() || getLastFlightToggle().hasNotPassed(8) || !chunkLoaded || packet.getPlayer().getAllowFlight() || packet.getPlayer().getActivePotionEffects().stream().anyMatch(effect -> effect.getType().getName().toLowerCase().contains("levi")) || packet.getPlayer().getGameMode().toString().contains("CREATIVE") || packet.getPlayer().getGameMode().toString().contains("SPEC") || lastVehicle.hasNotPassed() || getLastRiptide().hasNotPassed(10) || data.getLastLogin().hasNotPassed(50) || data.getVelocityProcessor().getLastVelocity().hasNotPassed(25));
    }

    private float getDistanceToNearestBlock(PlayerData data) {
        BoundingBox box = data.getBoundingBox().subtract(0, Math.min(2, Math.abs(serverYVelocity)), 0,0,0,0);

        val colliding = box.getCollidingBlockBoxes(data.getPlayer());

        return (float) colliding.stream().mapToDouble(cBox -> MathUtils.getDelta(to.getY(), cBox.maxY)).min().orElse(100);
    }

    public boolean isNearGround(PlayerData data, float amount) {
        BoundingBox box = data.getBoundingBox().grow(amount, amount, amount).subtract(0, 0, 0, 0, 1.6f, 0);

        boolean near = boxes.stream()
                .anyMatch(box2 -> box.collides(box2) && !BlockUtils.isSolid(BlockUtils.getBlock(box2.getMinimum().toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0)))
                        && box2.collidesVertically(box));
        return near;
    }

    public boolean isOnGround(PlayerData data, float amount) {
        BoundingBox box = data.getBoundingBox().grow(0.25f, 0, 0.25f).subtract(0, amount, 0, 0, 1.6f, 0);

        boolean near = boxes.stream()
                .anyMatch(box2 -> data.getBoundingBox().grow(1E-6f, 0.5f, 1E-6f).intersectsWithBox(box2) && box.collides(box2) && getTo().getY() + 0.1f >= box2.getMaximum().getY() && !BlockUtils.isSolid(BlockUtils.getBlock(box2.getMinimum().toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0)))
                        && box2.collidesVertically(box));
        return near;
    }

    public boolean isOnGround(BoundingBox inputBox, PlayerData data, float amount) {

        BoundingBox box = inputBox.subtract(0, amount, 0, 0, 0, 0);

        boolean onGround = box.getCollidingBlockBoxes(data.getPlayer()).stream().anyMatch(box::collidesVertically);
        return onGround;
    }


    private void predict(PlayerData data, float strafe, float forward, boolean beginning) {
        float f4 = 0.91F;

        if(beginning) {

            if (isClientOnGround()) {
                f4 = ReflectionsUtil.getFriction(BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()).clone().subtract(0, 1, 0))) * 0.91F;
            }

            float f = 0.16277136F / (f4 * f4 * f4);
            float f5;

            if (isClientOnGround()) {
                f5 = Atlas.getInstance().getBlockBoxManager().getBlockBox().getAiSpeed(data.getPlayer()) * f;
            } else {
                f5 = data.getPlayer().isSprinting() ? 0.026f : 0.02f;
            }

            this.moveFlying(strafe, forward, f5);
        } else {
            if(isClientOnGround()) {
                f4 = ReflectionsUtil.getFriction(BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()).clone().subtract(0, 1, 0))) * 0.91F;
            }

            this.motY *= 0.9800000190734863D;
            this.motX *= (double) f4;
            this.motZ *= (double) f4;
        }
    }

    public void moveFlying(float strafe, float forward, float friction) {
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(to.getYaw() * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(to.getYaw() * (float) Math.PI / 180.0F);
            this.motX += (double) (strafe * f2 - forward * f1);
            this.motZ += (double) (forward * f2 + strafe * f1);
        }
    }
    protected void bF(PlayerData data) {
        this.motY = 0.42;
        if (data.getPlayer().hasPotionEffect(PotionEffectType.JUMP)) {
            this.motY += (PlayerUtils.getPotionEffectLevel(data.getPlayer(), PotionEffectType.JUMP) * 0.1F);
        }

        if (data.getPlayer().isSprinting()) {
            float f = to.getYaw() * 0.017453292F;

            this.motX -= (MathHelper.sin(f) * 0.2F);
            this.motZ += (MathHelper.cos(f) * 0.2F);
        }
    }

}
