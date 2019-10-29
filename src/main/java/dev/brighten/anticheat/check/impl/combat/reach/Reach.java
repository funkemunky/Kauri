package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.RayCollision;
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
            List<Location> point = Collections.singletonList(data.pastLocation
                    .getPreviousLocation(data.lagInfo.transPing / 2))
                    .stream()
                    .map(kloc -> kloc.toLocation(data.getPlayer().getWorld())
                    .add(0, data.getPlayer().getEyeHeight(), 0))
                    .collect(Collectors.toList());

            List<BoundingBox> previousLocations = data.targetPastLocation
                    .getEstimatedLocation(data.lagInfo.transPing
                            , 150L)
                    .parallelStream()
                    .map(loc -> getHitbox(loc, data.target.getType()))
                    .collect(Collectors.toList());

            List<Double> reaches = new ArrayList<>();
            int collided = 0;

            for (Location origin : point) {
                RayCollision ray = new RayCollision(origin.toVector(), origin.getDirection());

                for (BoundingBox box : previousLocations) {
                    Tuple<Double, Double> result = new Tuple<>(0D,0D);

                    if(RayCollision.intersect(ray, box, result)) {
                        reaches.add(result.one);
                        collided++;
                    }
                }
            }

            if(collided > 1) {
                double reach = reaches.stream().mapToDouble(val -> val).min().orElse(0);

                if(reach > 3.1) {
                    if(collided > 3) {
                        vl++;
                    } vl+= 0.5;
                    if(vl > 2) {
                        flag("reach=" + reach + " collided=" + collided);
                    }
                } else vl-= vl > 0 ? 0.05 : 0;
                debug((reach > 3.1 ? Color.Green : "") + "reach=" + reach + " collided=" + collided + "vl=" + vl);
            }
        }
    }

    private static boolean checkParameters(ObjectData data, long timeStamp) {
        return timeStamp - data.playerInfo.lastAttackTimeStamp < 5
                && data.target != null
                && allowedEntities.contains(data.target.getType())
                && !data.playerInfo.inCreative;
    }

    private static BoundingBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);
        return new BoundingBox(loc.toVector(), loc.toVector())
                .grow((float)bounds.getX(), 0, (float)bounds.getZ())
                .add(0,0,0,0,(float)bounds.getY(),0)
                .grow(0.1f,0,0.1f).add(0,0,0,0,0.05f,0);
    }
}
