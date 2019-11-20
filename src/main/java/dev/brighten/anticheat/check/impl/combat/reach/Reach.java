package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.KLocation;
import dev.brighten.anticheat.utils.RayCollision;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
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
            long shit = timeStamp - 120;
            List<Location> point = data.pastLocation.getEstimatedLocation(0, Math.max(data.lagInfo.transPing, 150L))
                    .stream()
                    .map(kloc -> kloc.toLocation(data.getPlayer().getWorld())
                    .add(0, data.getPlayer().getEyeHeight(), 0))
                    .collect(Collectors.toList());

            List<BoundingBox> previousLocations = data.targetPastLocation
                    .getEstimatedLocation(0
                            , Math.max(150L, data.lagInfo.transPing))
                    .stream()
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

            if(collided > 1 && data.lagInfo.lastPacketDrop.hasPassed(1)) {
                double reach = reaches.stream().mapToDouble(val -> val).min().orElse(0);

                if(reach > 3.02) {
                    if((vl+= (collided > 4 ? 1 : 0.5f)) > 4) {
                        flag("reach=" + reach + " collided=" + collided);
                    }
                } else vl-= vl > 0 ? (data.lagInfo.lagging ? 0.025 : 0.02) : 0;
                debug((reach > 3.02 ? Color.Green : "") + "reach=" + reach + " collided=" + collided + "vl=" + vl);
            }
        }
    }

    private static boolean checkParameters(ObjectData data, long timeStamp) {
        return timeStamp - data.playerInfo.lastAttackTimeStamp < 5
                && data.target != null
                && !MiscUtils.containsIgnoreCase(data.getPlayer().getGameMode().toString(), "gamemode")
                && allowedEntities.contains(data.target.getType())
                && !data.playerInfo.inCreative;
    }

    private static BoundingBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);

        BoundingBox box = new BoundingBox(loc.toVector(), loc.toVector())
                .grow((float)bounds.getX(), 0, (float)bounds.getZ())
                .add(0,0,0,0,(float)bounds.getY(),0);

        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
            return box.grow(0.1f,0,0.1f);
        }

        return box;
    }
}