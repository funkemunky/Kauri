package dev.brighten.anticheat.check.impl.free.combat;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@CheckInfo(name = "Reach (A)", checkType = CheckType.HITBOX, punishVL = 4, description = "A simple distance check.",
        planVersion = KauriVersion.FREE, executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachA extends Check {

    private double buffer;

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
                    EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Setting(name = "maxDistance")
    public static double reachThreshold = 3.1;
    
    private final Queue<Entity> attacks = new LinkedList<>();
    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        reachA:
        {
            if (data.playerInfo.creative
                    || data.target == null
                    || data.target.getUniqueId() != data.target.getUniqueId()
                    || data.targetPastLocation.previousLocations.size() < 10
                    || data.playerInfo.inVehicle
                    || !allowedEntityTypes.contains(data.target.getType())) break reachA;


            List<KLocation> targetLocs = data.targetPastLocation
                    .getEstimatedLocationByIndex(data.lagInfo.transPing + 2,
                            3, 3);

            KLocation torigin = data.playerInfo.to.clone();

            torigin.y = 0;

            double distance = Double.MAX_VALUE;
            for (KLocation tloc : targetLocs) {
                double current = MiscUtils.getDistanceWithoutRoot(torigin, tloc);

                //If the calculated distance is smaller, we want to set it until we reach the smallest distance
                if(distance > current) {
                    distance = current;
                }
            }

            SimpleCollisionBox box = getHitbox(data.target, new KLocation(0,0,0));

            if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
                box = box.expand(0.1);
            }

            double horizontalDistance = box.min().setY(0).distance(box.max().setY(0));
            distance = Math.sqrt(distance) - horizontalDistance;

            if (distance > reachThreshold) {
                if (++buffer > 5) {
                    buffer = 5;
                    vl++;
                    flag("distance=%.2f buffer=%s", distance, buffer);
                }
            } else buffer -= buffer > 0 ? 0.075f : 0;

            debug("distance=%.3f boxes=%s hdist=%.3f buffer=%s lct=%s lts=%s",
                    distance, targetLocs.size(), horizontalDistance, buffer,
                    System.currentTimeMillis() - data.lagInfo.lastClientTrans,
                    data.playerInfo.lastTargetSwitch.getPassed());
        }
    }

    private static SimpleCollisionBox getHitbox(Entity entity, KLocation loc) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc, entity);
    }
}
