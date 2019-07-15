package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.CollisionAssessment;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.PastLocation;
import cc.funkemunky.anticheat.impl.config.MiscSettings;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MovementProcessor {
    private boolean lastFlight, serverPos, flight, inLiquid, liquidBelow, clientOnGround, serverOnGround, fullyInAir, inAir, hasJumped, nearLiquid, blocksOnTop, pistonsNear, onHalfBlock,
            onClimbable, onIce, collidesHorizontally, tookVelocity, inWeb, onSlime, onSlimeBefore, onSoulSand, isRiptiding, halfBlocksAround, isNearGround, isInsideBlock, blocksNear, blocksAround;
    private int airTicks, groundTicks, iceTicks, climbTicks, halfBlockTicks, soulSandTicks, blockAboveTicks, optifineTicks, liquidTicks, webTicks, yawZeroTicks, pitchZeroTicks;
    private float deltaY, lastDeltaXZ, slimeHeight, fallDistance, yawDelta, pitchDelta, lastYawDelta, lastPitchDelta, lastDeltaY, deltaXZ, lastServerYVelocity, serverYAcceleration, clientYAcceleration, lastClientYAcceleration, lastServerYAcceleration, cinematicYawDelta, cinematicPitchDelta, lastCinematicPitchDelta, lastCinematicYawDelta;
    private CustomLocation from, to;
    @Setter
    private float serverYVelocity;
    private PastLocation pastLocation = new PastLocation();
    private TickTimer lastRiptide = new TickTimer(6), lastVehicle = new TickTimer(4), lastFlightToggle = new TickTimer(10);
    private List<BoundingBox> boxes = new ArrayList<>();
    private List<Entity> entitiesAround = new ArrayList<>();
    private long lastTimeStamp, lagTicks, pitchGCD, yawGCD, offset = 16777216L;

    public void update(PlayerData data, WrappedInFlyingPacket packet) {
        Kauri.getInstance().getProfiler().start("data:MovementProcessor");
        val player = packet.getPlayer();

        if(player == null) return;
        val timeStamp = System.currentTimeMillis();
        boolean chunkLoaded = Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(player.getLocation());
        if (from == null || to == null) {
            from = new CustomLocation(0, 0, 0, 0, 0);
            to = new CustomLocation(0, 0, 0, 0, 0);
        }

        from = to.clone();
        clientOnGround = packet.isGround();

        if (packet.isPos()) {
            to.setX(packet.getX());
            to.setY(packet.getY());
            to.setZ(packet.getZ());

            data.setBoundingBox(new BoundingBox(to.toVector(), to.toVector()).grow(0.3f, 0, 0.3f).add(0,0,0,0,1.84f,0));

            if (chunkLoaded) {
                //Here we get the colliding boundingboxes surrounding the player.
                List<BoundingBox> box = boxes = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(player.getWorld(), data.getBoundingBox().grow(1.5f, 1.5f, 1.5f));

                CollisionAssessment assessment = new CollisionAssessment(data.getBoundingBox(), data);

                //There are some entities that are collide-able like boats but are not considered blocks.

                if(Atlas.getInstance().getCurrentTicks() % 2 == 0) {
                    entitiesAround = player.getNearbyEntities(1, 1, 1).stream().filter(entity -> (entity instanceof Vehicle && !entity.getType().toString().contains("MINECART")) || entity.getType().name().toLowerCase().contains("shulker")).collect(Collectors.toList());
                }

                entitiesAround.forEach(entity -> assessment.assessBox(ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(entity)), player.getWorld(), true));

                //Now we scrub through the colliding boxes for any important information that could be fed into detections.
                box.parallelStream().forEach(bb -> assessment.assessBox(bb, player.getWorld(), false));


                serverOnGround = assessment.isOnGround();
                blocksOnTop = assessment.isBlocksOnTop();
                collidesHorizontally = assessment.isCollidesHorizontally();
                nearLiquid = assessment.isNearLiquid();
                inLiquid = assessment.isInLiquid();
                liquidBelow = assessment.isLiquidBelow();
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
                isNearGround = assessment.isNearGround();
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
            tookVelocity = timeStamp - data.getVelocityProcessor().getLastVelocityTimestamp() < 1500L;

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
                data.setLastPacketDrop(timeStamp);
            } else lagTicks-= lagTicks > 0 ? 1 : 0;

            if(lagTicks > 0) {
                data.getLastPacketSkip().reset();
            }
            val block = BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()));
            val blockAbove = BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0));
            val blockBelow = BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()).clone().subtract(0, 1,0));

            isInsideBlock = block == null || blockAbove == null || BlockUtils.isSolid(block) || BlockUtils.isSolid(blockAbove);

            data.setBlockAbove(blockAbove);
            data.setBlockBelow(blockBelow);
            data.setBlockInside(block);

            if (isRiptiding = Atlas.getInstance().getBlockBoxManager().getBlockBox().isRiptiding(packet.getPlayer()))
                lastRiptide.reset();

            lastServerYVelocity = serverYVelocity;

            if (hasJumped) {
                serverYVelocity = Math.min(deltaY, MiscUtils.predicatedMaxHeight(data));
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
                serverYVelocity = deltaY;
            }

            if (getLastFlightToggle().hasNotPassed(3)) {
                serverYVelocity = deltaY;
            }

            if(data.getTeleportLocations().size() > 0) {
                val vecStream = data.getTeleportLocations().stream().filter(vec -> (MiscSettings.horizontalServerPos ? MathUtils.offset(vec, to.toVector()) : vec.distance(to.toVector())) < 1E-8).findFirst().orElse(null);

                if(vecStream != null) {
                    if(data.getTeleportLoc() != null && vecStream.distance(data.getTeleportLoc().toVector()) == 0) {
                        data.setTeleportPing(timeStamp - data.getTeleportTest());
                    } else {
                        data.setTeleportPing(timeStamp - data.getTeleportTest());
                    }
                    data.setLastServerPosStamp(timeStamp);
                    data.getTeleportLocations().remove(vecStream);
                    serverYVelocity = deltaY;
                    serverPos = true;
                    from = to;
                } else if(serverPos) {
                    serverPos = false;
                }
            }

            lastServerYAcceleration = serverYAcceleration;
            serverYAcceleration = serverYVelocity - lastServerYVelocity;

            //The MiscUtils#getDistanceToGround method is kind of heavy, so we only run it 4 times a second instead of 20.
            //We compensate for the loss of data by using the yDelta of the player to guess the distance.
            //This method should and won't be used for anything sensitive requiring precise data.
            //This is just used for preventing any false positives

            lastTimeStamp = timeStamp;

            if(data.isLoggedIn() && data.getLastLogin().hasPassed(2)) data.setLoggedIn(false);


            iceTicks = onIce ? Math.min(40, iceTicks + 2) : Math.max(0, iceTicks - 1);
            climbTicks = onClimbable ? Math.min(40, climbTicks + 1) : Math.max(0, climbTicks - 1);
            halfBlockTicks = onHalfBlock ? Math.min(40, halfBlockTicks + 2) : Math.max(0, halfBlockTicks - 1);
            blockAboveTicks = blocksOnTop ? Math.min(40, blockAboveTicks + 2) : Math.max(0, blockAboveTicks - 1);
            liquidTicks = nearLiquid ? Math.min(50, liquidTicks + 1) : Math.max(0, liquidTicks - 1);
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
            float smoothDelta = MathUtils.getDelta(yawDelta, smooth);
            float smoothDelta2 = MathUtils.getDelta(pitchDelta, smooth2);

            data.setCinematicMode((smoothDelta / yawDelta) < 0.1 || (smoothDelta2) < 0.02);

            yawGCD = MiscUtils.gcd((long) (yawDelta * offset), (long) (lastYawDelta * offset));
            pitchGCD = MiscUtils.gcd((long) (pitchDelta * offset), (long) (lastPitchDelta * offset));

            //Bukkit.broadcastMessage(smoothDelta + "," + smoothDelta2 + ": " + "(" + smoothDelta / yawDelta + "), " + "(" + (smoothDelta2 / pitchDelta) + "): " + data.isCinematicMode());
            if (data.isCinematicMode()) {
                optifineTicks += optifineTicks < 60 ? 1 : 0;
            } else if (optifineTicks > 0) {
                optifineTicks -= 3;
            }
            lastCinematicYawDelta = cinematicYawDelta;
            lastCinematicPitchDelta = cinematicPitchDelta;
            cinematicYawDelta = smoothDelta;
            cinematicPitchDelta = smoothDelta2;
        }

        if (to.getYaw() == from.getYaw()) {
            yawZeroTicks = Math.min(20, yawZeroTicks + 1);
        } else yawZeroTicks -= yawZeroTicks > 0 ? 1 : 0;

        if (to.getPitch() == from.getPitch()) {
            pitchZeroTicks = Math.min(20, pitchZeroTicks + 1);
        } else pitchZeroTicks -= pitchZeroTicks > 0 ? 1 : 0;
        //predict(data, .98f, .98f, false);

        pastLocation.addLocation(new CustomLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
        data.setGeneralCancel(data.isServerPos() || data.isLagging() || getLastFlightToggle().hasNotPassed(8) || !chunkLoaded || packet.getPlayer().getAllowFlight() || packet.getPlayer().getActivePotionEffects().stream().anyMatch(effect -> effect.getType().getName().toLowerCase().contains("levi")) || packet.getPlayer().getGameMode().toString().contains("CREATIVE") || packet.getPlayer().getGameMode().toString().contains("SPEC") || lastVehicle.hasNotPassed() || getLastRiptide().hasNotPassed(10) || data.getLastLogin().hasNotPassed(50) || data.getVelocityProcessor().getLastVelocity().hasNotPassed(25));
        Kauri.getInstance().getProfiler().stop("data:MovementProcessor");
    }
}
