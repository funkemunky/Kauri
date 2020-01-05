package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.RayCollision;
import dev.brighten.api.check.CheckType;
import lombok.val;
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

    private int verbose;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        vl-= vl > 0 ? 0.005 : 0;
    }

    @Packet
    public void onUse(WrappedInFlyingPacket packet, long timeStamp) {
        //debug("timeStamp=" + timeStamp + "ms");
        if(data.target == null || timeStamp - data.playerInfo.lastAttackTimeStamp > 55) return;

        val origins = data.pastLocation.getEstimatedLocation(0, Math.max(100L, Math.round(data.lagInfo.transPing / 2D)))
                .stream()
                .map(loc -> loc.toLocation(data.getPlayer().getWorld()).add(0, data.playerInfo.sneaking ? 1.54f : 1.62f, 0))
                .collect(Collectors.toList());

        val entityLoc = data.targetPastLocation
                .getEstimatedLocation(data.lagInfo.transPing, Math.max(250L, Math.round(data.lagInfo.transPing / 2D)));

        List<Double> distances = new ArrayList<>();

        origins.forEach(origin -> {
            RayCollision collision = new RayCollision(origin.toVector(), origin.getDirection());
            entityLoc.forEach(loc -> {
                Vector point = collision
                        .collisionPoint(getHitbox(loc, data.target.getType()));

                if(point != null) {
                    distances.add(point.distance(origin.toVector()));
                }
            });
        });


        if(distances.size() > 0) {
            val distance = distances.stream().mapToDouble(num -> num).min().orElse(0);

            if(distance > 3.0001 && data.lagInfo.lastPacketDrop.hasPassed(7)) {
                verbose++;
                if(verbose > 1) {
                    vl++;
                    flag("distance=%1 size=%2", MathUtils.round(distance, 3), distances.size());
                }
            } else verbose-= verbose > 0 ? data.lagInfo.lagging ? 0.025f : 0.01f : 0;
            debug("distance=" + distance + ", size=" + distances.size() + ", vl=" + verbose);
        }
    }

    private static BoundingBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);

        BoundingBox box = new BoundingBox(loc.toVector(), loc.toVector())
                .grow((float)bounds.getX(), 0, (float)bounds.getZ())
                .add(0,0,0,0,(float)bounds.getY(),0);

        return box.grow(0.1f,0.1f,0.1f);
    }
}