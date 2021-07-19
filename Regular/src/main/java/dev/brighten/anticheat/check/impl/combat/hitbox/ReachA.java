package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
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
import org.bukkit.util.Vector;

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

    private Hitboxes hitboxDetection;

    @Packet
    public void onFlying(WrappedInUseEntityPacket packet) {
        if(data.playerInfo.creative
                || data.targetPastLocation.previousLocations.size() < 10
                || packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                || !allowedEntityTypes.contains(packet.getEntity().getType())) return;

        List<KLocation> targetLocs = data.targetPastLocation
                .getEstimatedLocation(Kauri.INSTANCE.keepaliveProcessor.tick, data.lagInfo.transPing + 2, 3);

        KLocation torigin = data.playerInfo.to.clone(), forigin = data.playerInfo.from.clone();

        torigin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;
        forigin.y+= data.playerInfo.lsneaking ? 1.54f : 1.62f;

        Vector tovector = torigin.toVector();
        RayCollision tray = new RayCollision(tovector, MathUtils.getDirection(torigin)),
                fray = new RayCollision(forigin.toVector(), MathUtils.getDirection(forigin));

        int hits = 0, misses = 0, hitboxHits = 0;
        double distance = Double.MAX_VALUE;
        for (KLocation tloc : targetLocs) {
            SimpleCollisionBox tbox = getHitbox(data.target, tloc).expand(data.playerVersion
                    .isBelow(ProtocolVersion.V1_9) ? 0.1 : 0);

            Vector hitPoint = tray.collisionPoint(tbox);

            if(hitPoint != null) {
                hits++;
                distance = Math.min(distance, hitPoint.distanceSquared(tovector));
            } else misses++;

            hitboxes: {
                Hitboxes hitbox = getHitboxDetection();

                if(hitbox == null) {
                    Kauri.INSTANCE.getLogger().warning("Hitboxes is null within "
                            + data.getPlayer().getName() + " Reach (A) detection!");
                    break hitboxes;
                }

                SimpleCollisionBox expandedBox = tbox.copy().expand(0.1);
                if(hitPoint != null || tray.isIntersected(expandedBox) || fray.isIntersected(expandedBox))
                    hitboxHits++;
            }
        }

        hitboxes: {
            Hitboxes hitbox = getHitboxDetection();

            if(hitbox == null) {
                Kauri.INSTANCE.getLogger().warning("Hitboxes is null within "
                        + data.getPlayer().getName() + " Reach (A) detection!");
                break hitboxes;
            }

            if(hitboxHits == 0) {
                if(++hitbox.buffer > 5) {
                    hitbox.vl++;
                    hitbox.buffer = 5;
                    hitbox.flag("b=%.1f m=%s", hitbox.buffer, misses);
                }
            } else if(hitbox.buffer > 0) hitbox.buffer-= 0.2;
        }

        if(hits > 0)
        distance = Math.sqrt(distance) - 0.03; //We subtract 0.03 since our ray tracing isnt exactly 1-1
        else distance = -1;

        if(data.lagInfo.lastPacketDrop.isPassed(3)) {
            if (distance > 3.1) {
                if (++buffer > 6) {
                    buffer = 6;
                    vl++;
                    flag("distance=%.2f buffer=%s", distance, buffer);
                }
            } else buffer -= buffer > 0 ? 0.05 : 0;
        } else buffer-= buffer > 0 ? 0.02 : 0;

        debug("distance=%.3f h/m=%s,%s boxes=%s buffer=%s hbhits=%s",
                distance, hits, misses, targetLocs.size(), buffer, hitboxHits);
    }

    private static SimpleCollisionBox getHitbox(Entity entity, KLocation loc) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc, entity);
    }

    private Hitboxes getHitboxDetection() {
        if(hitboxDetection == null) hitboxDetection = (Hitboxes) data.checkManager.checks.get("Hitboxes");

        return hitboxDetection;
    }
}
