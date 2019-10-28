package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 10, executable = false)
public class Reach extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON,
            EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        vl-= vl > 0 ? 0.005 : 0;
    }

    @Packet
    public void onUse(WrappedInFlyingPacket packet, long timeStamp) {
        if(checkParameters(data, timeStamp)) {
            List<Location> point = Collections.singletonList(data.playerInfo.to)
                    .stream()
                    .map(kloc -> kloc.toLocation(data.getPlayer().getWorld())
                    .add(0, 1.54f, 0))
                    .collect(Collectors.toList());

            List<BoundingBox> previousLocations = Kauri.INSTANCE.dataManager.getData((Player)data.target)
                    .pastLocation
                    .getEstimatedLocation(data.lagInfo.transPing
                            , 200L)
                    .parallelStream()
                    .map(loc -> getHitbox(loc, data.target.getType()))
                    .collect(Collectors.toList());

            double distance = Math.min(6, data.target
                    .getLocation().toVector()
                    .distance(data.playerInfo.to.toVector()) * 1.2f);

            List<Double> collided = getColliding(distance, point, previousLocations);

            if(collided.size() > 5 && !data.lagInfo.lagging) {
                float calcDistance = (float) collided
                        .stream()
                        .mapToDouble(val -> val)
                        .min()
                        .orElse(-1D);

                if(calcDistance > 0) {
                    if(calcDistance > 3.02 && collided.size() > 24) {
                        if(vl++ > 4) {
                            flag("reach=" + calcDistance + " collided=" + collided.size());
                        }
                    } else vl-= vl > 0 ? 0.025 : 0;
                    debug("reach=" + calcDistance + " collided="
                            + collided.size() + "  vl=" + vl);
                }
            } else vl-= vl > 0 ? 0.01 : 0;
        }
    }

    private static boolean checkParameters(ObjectData data, long timeStamp) {
        return timeStamp - data.playerInfo.lastAttackTimeStamp < 5
                && data.target != null
                && !data.playerInfo.inCreative
                && !data.getPlayer().getGameMode().equals(GameMode.CREATIVE);
    }

    private List<Double> getColliding(double distance, List<Location> locs, List<BoundingBox> boxes) {
        List<Double> collided = new ArrayList<>();

        for (Location loc : locs) {
            RayTrace trace = new RayTrace(loc.toVector(), loc.getDirection());
            trace.traverse(0,
                    distance,
                    0.05,
                    0.02f,
                    2.4f,
                    3.4f)
                    .parallelStream()
                    .filter(vec -> boxes.stream().anyMatch(box -> box.collides(vec)))
                    .map(vec -> vec.distance(loc.toVector()))
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
