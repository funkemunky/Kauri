package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.List;

@CheckInfo(name = "Hitbox (A)", checkType = CheckType.HITBOX, punishVL = 4, description = "A simple distance check.",
        executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    private double buffer;

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
            EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Setting(name = "maxDistance")
    public static double reachThreshold = 3.1;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long now) {
        if(packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) return;

        Hitboxes: {
            if(data.playerInfo.creative
                    || data.targetPastLocation.previousLocations.size() < 10
                    || data.playerInfo.inVehicle
                    || data.target == null
                    || !allowedEntityTypes.contains(data.target.getType())) {
                debug("broken: " + data.targetPastLocation.previousLocations.size());
                break Hitboxes;
            }
            List<KLocation> targetLocs = data.targetPastLocation
                    .getEstimatedLocationByIndex(data.lagInfo.transPing + 2,
                            4, 4);

            KLocation torigin = data.playerInfo.to.clone(), forigin = data.playerInfo.from.clone();

            torigin.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;
            forigin.y+= data.playerInfo.lsneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;

            for (KLocation tloc : targetLocs) {
                SimpleCollisionBox tbox = getHitbox(data.target, tloc).expand(data.playerVersion
                        .isBelow(ProtocolVersion.V1_9) ? 0.1 : 0);
                final AxisAlignedBB vanillaBox = new AxisAlignedBB(tbox.expand(0.15));

                Vec3D intersectTo = vanillaBox.rayTrace(torigin.toVector(),
                        MathUtils.getDirection(torigin), 10),
                        intersecFrom = vanillaBox.rayTrace(forigin.toVector(),
                                MathUtils.getDirection(forigin), 10);

                if(intersectTo != null || intersecFrom != null) {
                    if(buffer > 0) buffer-= 0.2;
                    debug("hit: %.1f", buffer);
                    return;
                }
            }

            if(now - data.lagInfo.lastClientTrans < 150L
                    && data.lagInfo.lastPingDrop.isPassed(5) && ++buffer > 5) {
                vl++;
                flag("");
            }
            debug("missed: %.1f", buffer);
        }
    }

    private static SimpleCollisionBox getHitbox(Entity entity, KLocation loc) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc, entity);
    }
}
