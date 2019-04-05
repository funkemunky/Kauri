package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Vehicle;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MovementProcessor {
    private boolean lastFlight, flight, isLagging, clientOnGround, serverOnGround, fullyInAir, inAir, hasJumped, inLiquid, blocksOnTop, pistonsNear, onHalfBlock,
            onClimbable, onIce, collidesHorizontally, inWeb, onSlimeBefore, onSoulSand, isRiptiding, halfBlocksAround, isNearGround, isInsideBlock;
    private int airTicks, groundTicks, iceTicks, climbTicks, halfBlockTicks, soulSandTicks, blockAboveTicks, optifineTicks, liquidTicks, webTicks;
    private float deltaY, yawDelta, pitchDelta, lastYawDelta, lastPitchDelta, lastDeltaY, deltaXZ, distanceToGround, serverYVelocity, lastServerYVelocity, serverYAcceleration, clientYAcceleration, lastClientYAcceleration, lastServerYAcceleration, jumpVelocity, cinematicYawDelta, cinematicPitchDelta, lastCinematicPitchDelta, lastCinematicYawDelta;
    private CustomLocation from, to;
    private PastLocation pastLocation = new PastLocation();
    private TickTimer lastRiptide = new TickTimer(6), lastVehicle = new TickTimer(4), lastFlightToggle = new TickTimer(10);
    private List<BoundingBox> boxes = new ArrayList<>();
    private long lastTimeStamp;

    public void update(PlayerData data, WrappedInFlyingPacket packet) {
        val player = packet.getPlayer();
        val timeStamp = System.currentTimeMillis();
        boolean chunkLoaded = Atlas.getInstance().getBlockBoxManager().getBlockBox().isChunkLoaded(player.getLocation());
        Kauri.getInstance().getProfiler().start("MovementProcessor:update");
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
            data.setBoundingBox(ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(packet.getPlayer())));

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
                halfBlocksAround = assessment.getMaterialsCollided().stream().anyMatch(material -> material.toString().contains("STAIR") || material.toString().contains("STEP") || material.toString().contains("SLAB") || material.toString().contains("SNOW") || material.toString().contains("CAKE") || material.toString().contains("BED") || material.toString().contains("SKULL"));

                isNearGround = isNearGround(data, 1.5f);

                if (serverOnGround) {
                    groundTicks++;
                    airTicks = 0;

                    onSlimeBefore = assessment.isOnSlime();
                } else {
                    airTicks++;
                    groundTicks = 0;
                }
            }
            jumpVelocity = 0.42f + (PlayerUtils.getPotionEffectLevel(packet.getPlayer(), PotionEffectType.JUMP) * 0.1f);

            isLagging = MathUtils.getDelta(timeStamp, lastTimeStamp) < 5;

            lastDeltaY = deltaY;
            deltaY = (float) (to.getY() - from.getY());
            deltaXZ = (float) (cc.funkemunky.anticheat.api.utils.MiscUtils.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()));
            lastClientYAcceleration = clientYAcceleration;
            clientYAcceleration = deltaY - lastDeltaY;

            lastFlight = flight;
            flight = player.getAllowFlight();
            if (flight != lastFlight) {
                getLastFlightToggle().reset();
            }

            isInsideBlock = BlockUtils.isSolid(BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()))) || BlockUtils.isSolid(BlockUtils.getBlock(to.toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0)));

            if (isRiptiding = Atlas.getInstance().getBlockBoxManager().getBlockBox().isRiptiding(packet.getPlayer()))
                lastRiptide.reset();

            //Hear we use the client's ground packet being sent since whatever motion the client says it has
            //will line up with this since ground is sent along with positional packets (flying, poslook, pos, look)
            if (hasJumped) {
                hasJumped = false;
                inAir = true;
            } else if (clientOnGround) {
                inAir = false;
            } else if (!inAir) {
                hasJumped = true;
            }

            lastServerYVelocity = serverYVelocity;

            if (hasJumped) {
                serverYVelocity = Math.min(deltaY, 0.42f);
            } else if (inAir) {
                serverYVelocity -= 0.08f;
                serverYVelocity *= 0.98f;
            } else {
                serverYVelocity = 0;
            }

            if (getLastFlightToggle().hasNotPassed(3)) {
                serverYVelocity = deltaY;
            }

            lastServerYAcceleration = serverYAcceleration;
            serverYAcceleration = serverYVelocity - lastServerYVelocity;

            //The MiscUtils#getDistanceToGround method is kind of heavy, so we only run it 4 times a second instead of 20.
            //We compensate for the loss of data by using the yDelta of the player to guess the distance.
            //This method should and won't be used for anything sensitive requiring precise data.
            //This is just used for preventing any false positives.

            if (Kauri.getInstance().getCurrentTicks() % 4 == 0 && chunkLoaded) {
                distanceToGround = MiscUtils.getDistanceToGround(data, 40);
            } else {
                distanceToGround += deltaY;
            }

            lastTimeStamp = timeStamp;


            iceTicks = onIce ? Math.min(40, iceTicks + 1) : Math.max(0, iceTicks - 1);
            climbTicks = onClimbable ? Math.min(40, climbTicks + 1) : Math.max(0, climbTicks - 1);
            halfBlockTicks = onHalfBlock ? Math.min(40, halfBlockTicks + 2) : Math.max(0, halfBlockTicks - 1);
            blockAboveTicks = blocksOnTop ? Math.min(40, blockAboveTicks + 2) : Math.max(0, blockAboveTicks - 1);
            liquidTicks = inLiquid ? Math.min(50, liquidTicks + 1) : Math.max(0, liquidTicks - 1);
            soulSandTicks = onSoulSand ? Math.min(40, soulSandTicks + 1) : Math.max(0, soulSandTicks - 1);
            webTicks = inWeb ? Math.min(30, webTicks + 1) : Math.max(webTicks, webTicks - 1);
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

            val smoothDelta = MathUtils.getDelta(yawDelta, smooth);
            val smoothDelta2 = MathUtils.getDelta(pitchDelta, smooth2);

            data.setCinematicMode((smoothDelta / yawDelta) < 0.1 || (smoothDelta2) < 0.02);

            //Bukkit.broadcastMessage(smoothDelta + "," + smoothDelta2 + ": " + "(" + smoothDelta / yawDelta + "), " + "(" + (smoothDelta2 / pitchDelta) + "): " + data.isCinematicMode());
            if (data.isCinematicMode()) {
                optifineTicks += optifineTicks < 60 ? 1 : 0;
            } else if (optifineTicks > 0) {
                optifineTicks--;
            }
            lastCinematicYawDelta = cinematicYawDelta;
            lastCinematicPitchDelta = cinematicPitchDelta;
        }

        pastLocation.addLocation(new CustomLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
        data.setGeneralCancel(data.getLastServerPos().hasNotPassed(1) || (data.isLagging() && isLagging) || getLastFlightToggle().hasNotPassed(8) || !chunkLoaded || packet.getPlayer().getAllowFlight() || packet.getPlayer().getActivePotionEffects().stream().anyMatch(effect -> effect.getType().getName().toLowerCase().contains("levi")) || packet.getPlayer().getGameMode().toString().contains("CREATIVE") || packet.getPlayer().getGameMode().toString().contains("SPEC") || lastVehicle.hasNotPassed() || getLastRiptide().hasNotPassed(10) || data.getLastLogin().hasNotPassed(50) || data.getVelocityProcessor().getLastVelocity().hasNotPassed(40));
        Kauri.getInstance().getProfiler().stop("MovementProcessor:update");
    }

    public boolean isNearGround(PlayerData data, float amount) {
        Kauri.getInstance().getProfiler().start("MovementProcessor:isNearGround");
        BoundingBox box = data.getBoundingBox().grow(amount, amount, amount).subtract(0, 0, 0, 0, 1.6f, 0);

        boolean near = boxes.stream()
                .anyMatch(box2 -> box.collides(box2) && !BlockUtils.isSolid(BlockUtils.getBlock(box2.getMinimum().toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0)))
                        && box2.collidesVertically(box));
        Kauri.getInstance().getProfiler().start("MovementProcessor:isNearGround");
        return near;
    }

    public boolean isOnGround(PlayerData data, float amount) {
        Kauri.getInstance().getProfiler().start("MovementProcessor:isOnGround");
        BoundingBox box = data.getBoundingBox().grow(0.25f, 0, 0.25f).subtract(0, amount, 0, 0, 1.6f, 0);

        boolean near = boxes.stream()
                .anyMatch(box2 -> data.getBoundingBox().grow(1E-6f, 0.5f, 1E-6f).intersectsWithBox(box2) && box.collides(box2) && getTo().getY() + 0.1f >= box2.getMaximum().getY() && !BlockUtils.isSolid(BlockUtils.getBlock(box2.getMinimum().toLocation(data.getPlayer().getWorld()).clone().add(0, 1, 0)))
                        && box2.collidesVertically(box));
        Kauri.getInstance().getProfiler().stop("MovementProcessor:isOnGround");
        return near;
    }

    public boolean isOnGround(BoundingBox inputBox, PlayerData data, float amount) {
        Kauri.getInstance().getProfiler().start("MovementProcessor:isOnGround");

        BoundingBox box = inputBox.subtract(0, amount, 0, 0, 0, 0);

        boolean onGround = box.getCollidingBlockBoxes(data.getPlayer()).stream().anyMatch(box::collidesVertically);
        Kauri.getInstance().getProfiler().stop("MovementProcessor:isOnGround");
        return onGround;
    }


}
