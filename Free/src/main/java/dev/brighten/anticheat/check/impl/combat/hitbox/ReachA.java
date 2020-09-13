package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (A)", checkType = CheckType.HITBOX, punishVL = 5, description = "A simple distance check.")
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachA extends Check {

    private double buffer;

    private static List<EntityType> allowedEntityTypes = Arrays
            .asList(EntityType.ZOMBIE, EntityType.SHEEP, EntityType.BLAZE,
                    EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
                    EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Packet
    public void onFlying(WrappedInUseEntityPacket packet, long timeStamp) {
        if(data.playerInfo.creative
                || data.targetPastLocation.previousLocations.size() < 10
                || packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                || !allowedEntityTypes.contains(packet.getEntity().getType())) return;

        List<SimpleCollisionBox> targetBoxes = data.targetPastLocation
                .getEstimatedLocation(timeStamp, (data.lagInfo.transPing + 3) * 50, 100L)
                .stream().map(loc -> getHitbox(packet.getEntity(), loc)).collect(Collectors.toList());

        double distance = 69;

        val bounds = getHitbox(packet.getEntity(), new KLocation(0,0,0));

        if(bounds == null) return;
        for (SimpleCollisionBox target : targetBoxes) {
            distance = Math.min(distance, data.box.distance(target));
            //target.draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));
        }

        if(data.lagInfo.lastPacketDrop.hasPassed(3)) {
            if (distance > 3.3 && distance != 69) {
                if (++buffer > 6) {
                    vl++;
                    flag("distance=%v.2 buffer=%v", distance, buffer);
                }
            } else buffer -= buffer > 0 ? 0.1 : 0;
        } else buffer-= buffer > 0 ? 0.02 : 0;

        debug("distance=%v.3 boxes=%v buffer=%v", distance, targetBoxes.size(), buffer);
    }

    private static SimpleCollisionBox getHitbox(Entity entity, KLocation loc) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc, entity);
    }
}
