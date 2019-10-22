package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.RayTrace;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 15)
public class Reach extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON,
            EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER, EntityType.IRON_GOLEM);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        vl-= vl > 0 ? 0.01 : 0;
    }

    @Packet
    public void onUse(WrappedInFlyingPacket packet) {
        if(checkParameters(data)) {
            List<Location> rayTrace = data.pastLocation.getEstimatedLocation(0, 100L)
                    .stream()
                    .map(loc -> {
                        return loc.toLocation(data.getPlayer().getWorld())
                                .add(0, data.getPlayer().getEyeHeight(), 0);
                    })
                    .collect(Collectors.toList());

            List<BoundingBox> previousLocations = data.targetPastLocation
                    .getEstimatedLocation(data.lagInfo.transPing / 2
                            , 150L + (data.lagInfo.transPing - data.lagInfo.lastTransPing))
                    .parallelStream()
                    .map(loc -> getHitbox(loc, data.target.getType()))
                    .collect(Collectors.toList());

            double distance = Math.min(6, data.target
                    .getLocation().toVector()
                    .distance(data.playerInfo.to.toVector()) * 1.2f);

            List<Double> collided = getColliding(distance, rayTrace, previousLocations);

            if(collided.size() > 5) {
                float calcDistance = (float) collided
                        .stream()
                        .mapToDouble(val -> val)
                        .min()
                        .orElse(-1D);

                if(calcDistance > 0) {
                    if(calcDistance > 3 && collided.size() > 25) {
                        if(vl++ > 6) {
                            flag("reach=" + calcDistance + " collided=" + collided.size());
                        }
                    } else vl-= vl > 0 ? 0.025 : 0;
                    debug("reach=" + calcDistance + " collided="
                            + collided.size() + "  vl=" + vl);
                }
            } else vl-= vl > 0 ? 0.01 : 0;
        }
    }

    private static boolean checkParameters(ObjectData data) {
        return data.playerInfo.lastAttack.hasNotPassed(0)
                && data.target != null
                && allowedEntities.contains(data.target.getType())
                && !data.playerInfo.inCreative
                && !data.getPlayer().getGameMode().equals(GameMode.CREATIVE);
    }

    private List<Double> getColliding(double distance, List<Location> traces, List<BoundingBox> boxes) {
        List<Double> collided = new ArrayList<>();
        for (Location loc : traces) {
            RayTrace trace = new RayTrace(loc.toVector(), loc.getDirection());
            trace.traverse(
                    distance > 5 ? 3 : 0,
                    distance,
                    distance > 5 ? 0.1 : 0.05,
                    0.02f,
                    2.9f,
                    3.4f)
                    .parallelStream()
                    .filter(vec -> boxes.stream().anyMatch(box -> box.collides(vec)))
                    .map(vec -> vec.distance(data.playerInfo.to.toVector().add(new Vector(0, data.getPlayer().getEyeHeight(), 0))))
                    .sequential()
                    .forEach(collided::add);
        }

        return collided;
    }

    private static BoundingBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);
        return new BoundingBox(loc.toVector(), loc.toVector())
                .grow((float)bounds.getX(), 0, (float)bounds.getZ())
                .add(0,0,0,0,(float)bounds.getY(),0)
                .grow(0.1f,0.1f,0.1f);
    }
}
