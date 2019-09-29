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
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach", description = "A very accurate and fast 3.1 reach check.", type = CheckType.REACH, cancelType = CancelType.COMBAT, maxVL = 10, executable = true)
@Packets(packets = {Packet.Client.POSITION_LOOK, Packet.Client.POSITION, Packet.Client.LOOK, Packet.Client.FLYING})
@Init
public class Reach extends Check {

    private float vl;
    private long lastAttack;

    private static List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON, EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER, EntityType.IRON_GOLEM);

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();
        if(getData().getTarget() != null && getData().getLastAttack().hasNotPassed(0) && allowedEntities.contains(getData().getTarget().getType()) && !getData().getPlayer().getGameMode().equals(GameMode.CREATIVE) && getData().getLastLogin().hasPassed(5) && !move.isServerPos() && getData().getTransPing() < 450) {
            val to = move.getTo().toLocation(getData().getPlayer().getWorld()).add(0, getData().getPlayer().getEyeHeight(), 0);
            val calcDistance = getData().getTarget().getLocation().distance(to);

            if(timeStamp - lastAttack <= 5) {
                lastAttack = timeStamp;
                return;
            } else if(calcDistance > 20) {
                return;
            }

            val location = getData().getEntityPastLocation().
                    getEstimatedLocation(getData().getTransPing(), 
                            100L + Math.abs(getData().getTransPing() - getData().getLastTransPing()) * 2)
                    .stream().map(loc -> getHitbox(getData().getTarget(), loc)).collect(Collectors.toList());

            val locs = move.getPastLocation().getEstimatedLocation(0, 100L)
                    .stream()
                    .map(loc -> loc
                            .toLocation(getData().getPlayer().getWorld())
                            .clone()
                            .add(0, getData().getPlayer().getEyeHeight(), 0))
                    .collect(Collectors.toList());
            
            List<Double> distances = new ArrayList<>();

            locs.forEach(loc -> new RayTrace(loc.toVector(), loc.getDirection())
                    .traverse(0, calcDistance * 1.5f, 0.1,
                    0.02, 2.8, 3.5)
                    .stream()
                    .filter(vec -> location
                            .stream()
                            .anyMatch(box -> box.collides(vec)))
                    .mapToDouble(vec -> vec.distance(loc.toVector()))
                    .forEach(distances::add));

            float distance = (float)distances.stream().mapToDouble(dub -> dub)
                    .min().orElse(0.0D);

            if(distances.size() > 0) {
                if (distance > 3 && distances.size() > 25 && !getData().isLagging()) {
                    vl+= distance > 3.02 ? 1 : 0.5;
                    if (vl > 16) {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + distances.size(), true, true, AlertTier.CERTAIN);
                    } else if (vl > 9) {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + distances.size(), true, true, AlertTier.HIGH);
                    } else if(vl > 4) {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + distances.size(), true, true, vl > 6 ? 1 : 0, vl > 6 ? AlertTier.LIKELY : AlertTier.POSSIBLE);
                    } else {
                        flag("reach=" + distance + " vl=" + vl + " collided=" + distances.size(),true,true, 0, AlertTier.LOW);
                    }
                } else if(distance > 0.8) {
                    vl = Math.max(0, vl - 0.05f);
                }

                debug((distance > 3 && distances.size() > 25 && !getData().isLagging() ? Color.Green : "") + "distance=" + distance + " collided=" + distances.size() + " vl=" + vl + " eye=" + getData().getPlayer().getEyeHeight());
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
