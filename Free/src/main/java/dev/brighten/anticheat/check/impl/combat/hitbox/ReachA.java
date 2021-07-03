package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (A)", checkType = CheckType.HITBOX, punishVL = 5, description = "A simple distance check.",
        planVersion = KauriVersion.FREE)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachA extends Check {

    private double buffer;

    private static final List<EntityType> allowedEntityTypes = Arrays
            .asList(EntityType.ZOMBIE, EntityType.SHEEP, EntityType.BLAZE,
                    EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
                    EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Packet
    public void onFlying(WrappedInUseEntityPacket packet, long timeStamp) {
        if(data.playerInfo.creative
                || data.targetPastLocation.getPreviousLocations().size() < 10
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

        if(data.lagInfo.lastPacketDrop.isPassed(3)) {
            if (distance > 3.3 && distance != 69) {
                if (++buffer > 6) {
                    vl++;
                    flag("distance=%.2f buffer=%s", distance, buffer);
                }
            } else buffer -= buffer > 0 ? 0.1 : 0;
        } else buffer-= buffer > 0 ? 0.02 : 0;

        debug("distance=%.3f boxes=%s buffer=%s", distance, targetBoxes.size(), buffer);
    }

    private static SimpleCollisionBox getHitbox(Entity entity, KLocation loc) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc, entity);
    }
}
