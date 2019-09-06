package cc.funkemunky.anticheat.impl.checks.combat.reach;

import cc.funkemunky.anticheat.api.checks.*;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.RayTrace;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;

import java.util.*;
import java.util.stream.Collectors;

@Init
@CheckInfo(name = "Reach (Type B)", description = "New reach check.",
        type = CheckType.REACH, cancelType = CancelType.COMBAT, maxVL = 20)
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.FLYING, Packet.Client.LOOK})
public class ReachB extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON,
            EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER, EntityType.IRON_GOLEM);
    private float vl;

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(checkParameters(getData())) {
            val rayTrace = move.getPastLocation().getEstimatedLocation(0, 100L)
                    .stream()
                    .map(loc -> loc.toLocation(getData().getPlayer().getWorld())
                            .add(0, getData().getPlayer().getEyeHeight(), 0))
                    .collect(Collectors.toList());

            val previousLocations = getData().getEntityPastLocation()
                    .getEstimatedLocation(getData().getTransPing(), 150)
                    .stream()
                    .map(loc -> getData().getTargetBounds().add(loc.toVector()))
                    .collect(Collectors.toList());

            double distance = Math.min(6, getData().getTarget()
                    .getLocation().toVector()
                    .distance(move.getTo().toVector()) * 1.5f);

            List<Double> collided = getColliding(distance, rayTrace, previousLocations);

            if(collided.size() > 5) {
                val doubleMap = collided.stream().mapToDouble(val -> val);
                double avg = doubleMap.average().orElse(-1D);
                double calcDistance = doubleMap.min().orElse(-1D);

                if(calcDistance > 0) {
                    if(calcDistance > 3) {
                        if(vl++ > 8) {
                            flag("reach=" + calcDistance + " collided=" + collided.size(), true, true, AlertTier.HIGH);
                        }
                    } else vl-= vl > 0 ? 0.05 : 0;
                    debug("reach=" + calcDistance + " collided="
                            + collided.size() + " avg=" + avg + "  vl=" + vl);
                }
            } else vl-= vl > 0 ? 0.02 : 0;
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }

    private static boolean checkParameters(PlayerData data) {
        return data.getLastAttack().hasNotPassed(0)
                && allowedEntities.contains(Objects.requireNonNull(data.getTarget()).getType())
                && !data.isCreativeMode();
    }

    private static List<Double> getColliding(double distance, List<Location> traces, List<BoundingBox> boxes) {
        List<Double> collided = new ArrayList<>();
        for (Location loc : traces) {
            RayTrace trace = new RayTrace(loc.toVector(), loc.getDirection());
            trace.traverse(
                    distance > 5 ? 3 : 0,
                    distance,
                    0.1,
                    2.8f,
                    0.02f,
                    3.6f)
                    .parallelStream()
                    .filter(vec -> boxes.stream().anyMatch(box -> box.collides(vec)))
                    .map(vec -> vec.distance(loc.toVector()))
                    .forEach(collided::add);
        }

        return collided;
    }
}
