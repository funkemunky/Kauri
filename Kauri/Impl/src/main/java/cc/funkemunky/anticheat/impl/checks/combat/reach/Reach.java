package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
    private static float reachThreshold = 3f;

    @Setting(name = "threshold.collided")
    private static int collidedThreshold = 22;

    @Setting(name = "threshold.colidedMin")
    private static int collidedMin = 8;

    @Setting(name = "threshold.bypassCollidedReach")
    private static float bypassColReach = 4f;

    @Setting(name = "threshold.vl.certain")
    private static int certainThreshold = 16;

    @Setting(name = "threshold.vl.high")
    private static int highThreshold = 6;

    private List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER, EntityType.IRON_GOLEM);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if(getData().getTarget() != null && getData().getLastAttack().hasNotPassed(0) && allowedEntities.contains(getData().getTarget().getType()) && !getData().getPlayer().getGameMode().equals(GameMode.CREATIVE) && getData().getLastLogin().hasPassed(5) && !move.isServerPos() && getData().getTransPing() < 450) {
            long range = (move.getYawDelta() > 5 ? 150 : 100) + Math.abs(getData().getTransPing() - getData().getLastTransPing()) * 2;
            val location = getData().getEntityPastLocation().getEstimatedLocation(getData().getTransPing(), range);
            val to = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, getData().getPlayer().getEyeHeight(), 0);

            val calcDistance = getData().getTarget().getLocation().distance(to);

            if(timeStamp - lastAttack <= 5) {
                lastAttack = timeStamp;
                return;
            } else if(calcDistance > 20) {
                return;
            }

            val locs = move.getPastLocation().getEstimatedLocation(0, ((getData().getTarget().getVelocity().getY() <= 0 && move.getDeltaXZ() > cc.funkemunky.anticheat.api.utils.MiscUtils.getBaseSpeed(getData())) ? 50 : (move.getYawDelta() > 5 ? 50 : 0)) + (getData().getTransPing() - getData().getLastTransPing()) * 2).stream().map(loc -> loc.add(0, getData().getPlayer().getEyeHeight(), 0L)).collect(Collectors.toList());
            List<Vector> traverse = new ArrayList<>();
            locs.stream().map(loc -> new RayTrace(loc.toVector(), loc.toLocation(getData().getPlayer().getWorld()).getDirection()).traverse(0, calcDistance * 2f, 0.1, 0.02, 2.8, 3.7)).forEach(traverse::addAll);
            val collided = traverse.stream().filter((vec) -> location.stream().anyMatch((loc) -> getHitbox(getData().getTarget(), loc).collides(vec))).collect(Collectors.toList());

            float distance = (float) collided.stream().mapToDouble((vec) -> vec.distance(to.toVector()))
                    .min().orElse(0.0D);

            if(collided.size() > 0) {
                if (distance > reachThreshold && (collided.size() > collidedThreshold || distance > bypassColReach) && collided.size() > collidedMin && !getData().isLagging()) {
                    vl++;
                    if (vl > certainThreshold) {
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

                debug((distance > reachThreshold && (collided.size() > collidedThreshold || distance > bypassColReach) && collided.size() > collidedMin && !getData().isLagging() ? Color.Green : "") + "distance=" + distance + " collided=" + collided.size() + " vl=" + vl + " range=" + range + " target=" + getData().getTarget().getVelocity().getY() + " eye=" + getData().getPlayer().getEyeHeight());
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
