package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Vehicle;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
public class MovementProcessor {
    private boolean clientOnGround, serverOnGround, fullyInAir, inAir, hasJumped, inLiquid, blocksOnTop, pistonsNear, onHalfBlock,
            onClimbable, onIce, collidesHorizontally, inWeb, onSlimeBefore, onSoulSand, isRiptiding, halfBlocksAround;
    private int airTicks, groundTicks, iceTicks, climbTicks, halfBlockTicks, soulSandTicks, blockAboveTicks, optifineTicks, liquidTicks, webTicks;
    private float deltaY, lastDeltaY, deltaXZ, distanceToGround, serverYVelocity, lastServerYVelocity, serverYAcceleration, clientYAcceleration, jumpVelocity, cinematicYawDelta, cinematicPitchDelta, yawDelta, pitchDelta, lastYawDelta, lastPitchDelta;
    private CustomLocation from, to;
    private PastLocation pastLocation = new PastLocation();
    private TickTimer lastRiptide = new TickTimer(6);

    public void update(PlayerData data, WrappedInFlyingPacket packet) {

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
            data.setBoundingBox(new BoundingBox(to.toVector(), to.toVector().add(new Vector(0, 1.85, 0))).grow(0.3f, 0, 0.3f));

            //Here we get the colliding boundingboxes surrounding the player.
            List<BoundingBox> box = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(packet.getPlayer().getWorld(), data.getBoundingBox().grow(0.5f, 0.35f, 0.5f).subtract(0, 0.5f, 0, 0, 0, 0));

            CollisionAssessment assessment = new CollisionAssessment(data.getBoundingBox(), data);

            //There are some entities that are collide-able like boats but are not considered blocks.
            data.getPlayer().getNearbyEntities(1, 1, 1).stream().filter(entity -> entity instanceof Vehicle).forEach(entity -> assessment.assessBox(ReflectionsUtil.toBoundingBox(ReflectionsUtil.getBoundingBox(entity)), data.getPlayer().getWorld(), true));

            //Now we scrub through the colliding boxes for any important information that could be fed into detections.
            box.forEach(bb -> assessment.assessBox(bb, data.getPlayer().getWorld(), false));

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
            onSoulSand = assessment.getMaterialsCollided().contains(Material.SOUL_SAND);
            halfBlocksAround = assessment.getMaterialsCollided().stream().anyMatch(material -> material.toString().contains("STAIR") || material.toString().contains("STEP") || material.toString().contains("SLAB") || material.toString().contains("SNOW") || material.toString().contains("CAKE") || material.toString().contains("BED") || material.toString().contains("SKULL"));

            jumpVelocity = 0.42f + (PlayerUtils.getPotionEffectLevel(packet.getPlayer(), PotionEffectType.JUMP) * 0.1f);

            if (serverOnGround) {
                groundTicks++;
                airTicks = 0;

                onSlimeBefore = assessment.isOnSlime();
            } else {
                airTicks++;
                groundTicks = 0;
            }

            lastDeltaY = deltaY;
            deltaY = (float) (to.getY() - from.getY());
            deltaXZ = (float) (Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()));
            clientYAcceleration = deltaY - lastDeltaY;

            if(isRiptiding = Atlas.getInstance().getBlockBoxManager().getBlockBox().isRiptiding(packet.getPlayer())) {
                lastRiptide.reset();
            }

            //Hear we use the client's ground packet being sent since whatever motion the client says it has
            //will line up with this since ground is sent along with positional packets (flying, poslook, pos, look)
            if (hasJumped) {
                hasJumped = false;
                inAir = true;
            } else if (serverOnGround) {
                inAir = false;
            } else if (!inAir) {
                hasJumped = true;
            }

            lastServerYVelocity = serverYVelocity;

            if (hasJumped) {
                serverYVelocity = jumpVelocity;
            } else if (inAir) {
                serverYVelocity -= 0.08f;
                serverYVelocity *= 0.98f;
            } else {
                serverYVelocity = 0;
            }

            serverYAcceleration = serverYVelocity - lastServerYVelocity;

            //The MiscUtils#getDistanceToGround method is kind of heavy, so we only run it 4 times a second instead of 20.
            //We compensate for the loss of data by using the yDelta of the player to guess the distance.
            //This method should and won't be used for anything sensitive requiring precise data.
            //This is just used for preventing any false positives.

            //TODO Test the new getDistanceToGround method since it was recoded to be lighter.
            if (Kauri.getInstance().getCurrentTicks() % 4 == 0) {
                distanceToGround = MiscUtils.getDistanceToGround(data, 40);
            } else {
                distanceToGround += deltaY;
            }

            iceTicks = onIce ? Math.min(40, iceTicks + 1) : Math.max(0, iceTicks - 1);
            climbTicks = onClimbable ? Math.min(40, climbTicks + 1) : Math.max(0, climbTicks - 1);
            halfBlockTicks = onHalfBlock ? Math.min(40, halfBlockTicks + 2) : Math.max(0, halfBlockTicks - 1);
            blockAboveTicks = blocksOnTop ? Math.min(40, blockAboveTicks + 2) : Math.max(0, blockAboveTicks - 1);
            liquidTicks = inLiquid ? Math.min(50, liquidTicks + 1) : Math.max(0, liquidTicks - 1);
            soulSandTicks = onSoulSand ? Math.min(40, soulSandTicks + 1) : Math.max(0, soulSandTicks - 1);
            webTicks = inWeb ? Math.min(30, webTicks + 1) : Math.max(webTicks, webTicks - 1);
        }

        if (packet.isLook()) {
            to.setYaw(packet.getYaw());
            to.setPitch(packet.getPitch());

            //Algorithm stripped from the MC client which calculates the deceleration of rotation when using cinematic/optifine zoom.
            //Used to separate a legitimate aimbot-like rotation from a cheat.
            this.lastYawDelta = yawDelta;
            float yawDelta = MathUtils.getDelta(to.getYaw(), from.getYaw()), pitchDelta = MathUtils.getDelta(to.getPitch(), from.getPitch());
            float yawShit = MiscUtils.convertToMouseDelta(yawDelta), pitchShit = MiscUtils.convertToMouseDelta(pitchDelta);
            float smooth = data.getYawSmooth().smooth(yawShit, yawShit * 0.05f), smooth2 = data.getPitchSmooth().smooth(pitchShit, pitchShit * 0.05f);

            data.setCinematicMode((cinematicYawDelta = MathUtils.getDelta(smooth, yawShit)) < 0.1f && (cinematicPitchDelta = MathUtils.getDelta(smooth2, pitchShit)) < 0.08f);

            if (data.isCinematicMode()) {
                optifineTicks+= optifineTicks < 60 ? 1 : 0;
            } else if(optifineTicks > 0) {
                optifineTicks--;
            }
        }

        pastLocation.addLocation(new CustomLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
        data.setGeneralCancel(data.isAbleToFly() || packet.getPlayer().getAllowFlight() || PlayerUtils.isGliding(data.getPlayer()) || data.getPlayer().getVehicle() != null || data.isCreativeMode() || isRiptiding || data.getLastLogin().hasNotPassed(50) || data.getVelocityProcessor().getLastVelocity().hasNotPassed(40));
    }
}
