package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.*;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach", description = "A very accurate and fast 3.1 reach check.", type = CheckType.REACH, cancelType = CancelType.COMBAT, maxVL = 10, executable = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LOOK, Packet.Client.FLYING})
@Init
public class Reach extends Check {

    private float vl;
    private long lastAttack;

    @Setting(name = "threshold.reach")
    private float reachThreshold = 3f;

    @Setting(name = "threshold.collided")
    private int collidedThreshold = 20;

    @Setting(name = "threshold.colidedMin")
    private int collidedMin = 6;

    @Setting(name = "threshold.bypassCollidedReach")
    private float bypassColReach = 4f;

    @Setting(name = "threshold.vl.certain")
    private int certainThreshold = 16;

    @Setting(name = "threshold.vl.high")
    private int highThreshold = 6;

    private List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER, EntityType.IRON_GOLEM);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if(getData().getTarget() != null && allowedEntities.contains(getData().getTarget().getType()) && getData().getLastAttack().hasNotPassed(0) && !getData().getPlayer().getGameMode().equals(GameMode.CREATIVE) && getData().getTarget() != null && getData().getLastAttack().hasNotPassed(0) && getData().getLastLogin().hasPassed(5) && !move.isServerPos() && getData().getTransPing() < 450) {
            long range = 200 + Math.abs(getData().getTransPing() - getData().getLastTransPing()) * 3;
            val location = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), range);
            val to = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, getData().getPlayer().getEyeHeight(), 0);
            val trace = new RayTrace(to.toVector(), to.getDirection());

            val calcDistance = getData().getTarget().getLocation().distance(to);

            if(timeStamp - lastAttack <= 5) {
                lastAttack = timeStamp;
                return;
            } else if(calcDistance > 40) {
                //flag(calcDistance + ">-20", true, false, AlertTier.LIKELY);
                return;
            }

            val traverse = trace.traverse(0, calcDistance * 1.25f, 0.1, 0.02, 2.8, 4);
            val collided = traverse.stream().filter((vec) -> location.stream().anyMatch((loc) -> getHitbox(getData().getTarget(), loc).collides(vec))).collect(Collectors.toList());

            float distance = (float) collided.stream().mapToDouble((vec) -> vec.distance(to.toVector()))
                    .min().orElse(0.0D);

            if(getData().getTarget().getVelocity().getY() == 0 || PlayerUtils.getPotionEffectLevel(getData().getPlayer(), PotionEffectType.SPEED) > 0) {
                distance-= move.getDeltaXZ() / 1.55f;
            }

            if(collided.size() > 0) {
                if (distance > reachThreshold && (collided.size() > collidedThreshold || distance > bypassColReach) && collided.size() > collidedMin && !getData().isLagging()) {
                    if ((vl+= distance > 3.02 ? 1f : .5f) > certainThreshold) {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + collided.size(), true, true, AlertTier.CERTAIN);
                    } else if (vl > highThreshold) {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + collided.size(), true, true, AlertTier.HIGH);
                    } else if(vl > 4) {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + collided.size(), true, false, vl > 6 ? AlertTier.LIKELY : AlertTier.POSSIBLE);
                    } else {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + collided.size(),true,false,AlertTier.LOW);
                    }
                } else if(distance > 0.8) {
                    vl = Math.max(0, vl - 0.05f);
                }

                debug((distance > reachThreshold && (collided.size() > collidedThreshold || distance > bypassColReach) && collided.size() > collidedMin && !getData().isLagging() ? Color.Green : "") + "distance=" + distance + " collided=" + collided.size() + " vl=" + vl + " range=" + range + " target=" + getData().getTarget().getVelocity().getY());
            }
            lastAttack = timeStamp;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private BoundingBox getHitbox(LivingEntity entity, CustomLocation l) {
        Vector dimensions = MiscUtils.entityDimensions.getOrDefault(entity.getType(), new Vector(0.35F, 1.85F, 0.35F));
        return (new BoundingBox(l.toVector(), l.toVector())).grow(0.15F, 0.15F, 0.15F).grow((float)dimensions.getX(), 0.0F, (float)dimensions.getZ()).add(0.0F, 0.0F, 0.0F, 0.0F, (float)dimensions.getY(), 0.0F);
    }
}
