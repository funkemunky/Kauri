package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 10)
@Cancellable(cancelType = CancelType.ATTACK)
public class Reach extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON,
            EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER);

    private float verbose;

    @Packet
    public void onUse(WrappedInFlyingPacket packet, long timeStamp) {
        if(data.playerInfo.lastAttack.hasPassed(0) || data.target == null
                || !allowedEntities.contains(data.target.getType()) || data.playerInfo.creative) return;

        val originList = Arrays.asList(data.playerInfo.to.clone(), data.playerInfo.from.clone());


        val entityLoc = (data.targetData != null ? data.targetData.pastLocation : data.targetPastLocation)
                .getEstimatedLocation(data.lagInfo.transPing, Math.max(225L, Math.round(data.lagInfo.transPing / 2D)));

        List<Double> distances = new ArrayList<>();

        for (KLocation origin : originList) {
            origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;
            RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));
            entityLoc.forEach(loc -> {
                Vector point = collision
                        .collisionPoint((data.targetData != null
                                ? getHitbox(loc) : getHitbox(loc, data.target.getType())));
                if(point != null) {
                    distances.add(point.distance(origin.toVector()));
                }
            });
        }

        int size = distances.size();
        if(size > 0) {
            val distance = distances.stream().mapToDouble(num -> num).min().orElse(0) - 0.02;

            if(distance > 3.06 && size > 2
                    && data.lagInfo.lastPacketDrop.hasPassed(2)) {
                verbose+= size > 4 ? 1 : 0.5;
                if(verbose > 5) {
                    vl++;
                    flag("distance=%1 size=%2 origin=%3", MathUtils.round(distance, 3),
                            distances.size(), entityLoc.size());
                }
            } else verbose-= verbose > 0 ? data.lagInfo.lagging ? 0.1f : 0.02f : 0;
            debug("distance=" + distance + ", size=" + distances.size() + ", vl=" + verbose);
        }
    }

    private static SimpleCollisionBox getHitbox(KLocation loc) {
        return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(0.4f, 0.1f, 0.4f)
                .expandMax(0,1.8,0);
    }

    private static SimpleCollisionBox getHitbox(KLocation loc, EntityType type) {
        if(type.equals(EntityType.PLAYER)) {
            return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(0.4,0,0.4)
                    .expandMax(0,1.8,0);
        } else {
            Vector bounds = MiscUtils.entityDimensions.get(type);

            SimpleCollisionBox box = new SimpleCollisionBox(loc.toVector(), loc.toVector())
                    .expand(bounds.getX(), 0, bounds.getZ())
                    .expandMax(0, bounds.getY(),0);

            return box.expand(0.1,0.1,0.1);
        }
    }
}