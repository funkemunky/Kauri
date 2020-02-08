package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
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

@CheckInfo(name = "Reach", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 10)
@Cancellable(cancelType = CancelType.ATTACK)
public class Reach extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(EntityType.PLAYER, EntityType.SKELETON,
            EntityType.ZOMBIE, EntityType.PIG_ZOMBIE, EntityType.VILLAGER);

    private float verbose;


    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        vl-= vl > 0 ? 0.005 : 0;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        if(data.target == null || !allowedEntities.contains(data.target.getType())
                || data.playerInfo.creative) return;

        val origin = data.playerInfo.to.toLocation(data.getPlayer().getWorld())
                .add(0, data.playerInfo.sneaking ? 1.54f : 1.62f, 0);

        val entityLoc = (data.targetData != null ? data.targetData.pastLocation : data.targetPastLocation)
                .getEstimatedLocation(data.lagInfo.transPing, Math.max(200, Math.round(data.lagInfo.transPing / 2D)));

        List<Double> distances = new ArrayList<>();

        RayCollision collision = new RayCollision(origin.toVector(), origin.getDirection());
        entityLoc.forEach(loc -> {
            Vector point = collision
                    .collisionPoint((data.targetData != null
                            ? getHitbox(loc) : getHitbox(loc, data.target.getType())));

            if(point != null) {
                distances.add(point.distance(origin.toVector()));
            }
        });

        int size = distances.size();
        if(size > 0) {
            val distance = distances.stream().mapToDouble(num -> num).min().orElse(0);

            if(distance > 3.02 && size > 2
                    && data.lagInfo.lastPacketDrop.hasPassed(2)) {
                verbose+= size > 4 ? 1 : 0.5f;
                if(verbose > 3) {
                    vl++;
                    flag("distance=%1 size=%2", MathUtils.round(distance, 3), distances.size());
                }
            } else verbose-= verbose > 0 ? data.lagInfo.lagging ? 0.05f : 0.025f : 0;
            debug("distance=" + distance + ", size=" + distances.size() + ", vl=" + verbose);
        }
    }

    private static SimpleCollisionBox getHitbox(KLocation loc) {
        return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(0.4f, 0.1f, 0.4f)
                .expandMax(0,1.8,0);
    }

    private static SimpleCollisionBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);

        SimpleCollisionBox box = new SimpleCollisionBox(loc.toVector(), loc.toVector())
                .expand(bounds.getX(), 0, bounds.getZ())
                .expandMax(0, bounds.getY(),0);

        return box.expand(0.1f,0.1f,0.1f);
    }
}