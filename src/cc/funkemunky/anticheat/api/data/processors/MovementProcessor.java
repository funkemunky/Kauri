package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.*;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.PlayerUtils;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.Getter;
import org.bukkit.entity.Vehicle;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
@Setting
public class MovementProcessor {
    private boolean clientOnGround, serverOnGround, fullyInAir, inAir, hasJumped, inLiquid, blocksOnTop, pistonsNear, onHalfBlock,
            onClimbable, onIce, collidesHorizontally, inWeb, onSlimeBefore;
    private int airTicks, groundTicks, iceTicks, climbTicks, halfBlockTicks, blockAboveTicks, optifineTicks;
    private float deltaY, deltaXZ, distanceToGround, serverYVelocity, lastServerYVelocity, serverYAcceleration, jumpVelocity;
    private CustomLocation from, to;
    private PastLocation pastLocation = new PastLocation();

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
            List<BoundingBox> box = Atlas.getInstance().getBlockBoxManager().getBlockBox().getCollidingBoxes(packet.getPlayer().getWorld(), data.getBoundingBox().grow(0.5f, 0.1f, 0.5f).subtract(0, 0.5f, 0, 0, 0, 0));

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

            jumpVelocity = 0.42f + (PlayerUtils.getPotionEffectLevel(packet.getPlayer(), PotionEffectType.JUMP) * 0.1f);

            if (serverOnGround) {
                groundTicks++;
                airTicks = 0;

                onSlimeBefore = assessment.isOnSlime();
            } else {
                airTicks++;
                groundTicks = 0;
            }

            deltaY = (float) (to.getY() - from.getY());
            deltaXZ = (float) (Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()));

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
        }

        if (packet.isLook()) {
            to.setYaw(packet.getYaw());
            to.setPitch(packet.getPitch());

            //Algorithm stripped from the MC client which calculates the deceleration of rotation when using cinematic/optifine zoom.
            //Used to separate a legitimate aimbot-like rotation from a cheat.
            float yawDelta = MathUtils.getDelta(to.getYaw(), from.getYaw()), pitchDelta = MathUtils.getDelta(to.getPitch(), from.getPitch());
            float yawShit = MiscUtils.convertToMouseDelta(yawDelta), pitchShit = MiscUtils.convertToMouseDelta(pitchDelta);
            float smooth = data.getYawSmooth().smooth(yawShit, yawShit * 0.05f), smooth2 = data.getPitchSmooth().smooth(pitchShit, pitchShit * 0.05f);

            data.setCinematicMode(MathUtils.getDelta(smooth, yawShit) < 0.08f || MathUtils.getDelta(smooth2, pitchShit) < 0.04f);

            if (data.isCinematicMode()) {
                optifineTicks++;
            } else if (optifineTicks > 0) {
                optifineTicks--;
            }
        }

        data.lagTicks = data.isLagging() ? Math.min(100, data.lagTicks + 5) : Math.max(0, data.lagTicks - 1);
        pastLocation.addLocation(new CustomLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch()));
        data.setGeneralCancel(data.isAbleToFly() || packet.getPlayer().getAllowFlight() || data.isCreativeMode() || data.isRiptiding() || data.getLastLogin().hasNotPassed(50) || data.getVelocityProcessor().getLastVelocity().hasNotPassed(40));
    }
}
