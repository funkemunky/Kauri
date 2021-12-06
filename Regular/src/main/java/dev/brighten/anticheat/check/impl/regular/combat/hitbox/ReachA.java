package dev.brighten.anticheat.check.impl.regular.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.List;

@CheckInfo(name = "Reach (A)", checkType = CheckType.HITBOX, punishVL = 7, description = "A simple distance check.",
        planVersion = KauriVersion.FREE, executable = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachA extends Check {

    private double buffer;

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
                    EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    private boolean attacked;
    private int cancelTicks;
    private int transBetweenFlying;
    @Packet
    public boolean onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) attacked = true;

        if(cancelTicks > 0) {
            cancelTicks--;
            return true;
        }

        return false;
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        transBetweenFlying = 0;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        ++transBetweenFlying;
        reachA: {
            if(data.playerInfo.creative
                    || data.targetPastLocation.previousLocations.size() < 10
                    || !attacked
                    || data.target == null
                    || !allowedEntityTypes.contains(data.target.getType())) break reachA;

            if(!data.getEntityLocation(data.target).sentTeleport) break reachA;


            List<KLocation> targetLocs = data.entityLocPastLocation
                    .getEstimatedLocationByIndex(data.lagInfo.transPing + 1 + transBetweenFlying,
                            3, 3);

            KLocation torigin = data.playerInfo.to.clone(), forigin = data.playerInfo.from.clone();

            torigin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;
            forigin.y+= data.playerInfo.lsneaking ? 1.54f : 1.62f;

            int hits = 0, misses = 0, hitboxHits = 0;
            double distance = Double.MAX_VALUE;
            for (KLocation tloc : targetLocs) {
                SimpleCollisionBox tbox = getHitbox(data.target, tloc).expand(data.playerVersion
                        .isBelow(ProtocolVersion.V1_9) ? 0.1 : 0);
                final AxisAlignedBB vanillaBox = new AxisAlignedBB(tbox);
                Vec3D intersect = vanillaBox.rayTrace(torigin.toVector(), MathUtils.getDirection(torigin), 10);

                if(intersect != null) {
                    hits++;
                    distance = Math.min(distance, intersect.distanceSquared(new Vec3D(torigin.x, torigin.y, torigin.z)));
                } else misses++;

                hitboxes: {
                    Hitboxes hitbox = find(Hitboxes.class);

                    if(hitbox == null || !hitbox.isEnabled()) {
                        break hitboxes;
                    }

                    SimpleCollisionBox expandedBox = tbox.copy().expand(0.25);

                    final AxisAlignedBB expanded = new AxisAlignedBB(expandedBox);
                    Vec3D intersect2 = expanded.rayTrace(torigin.toVector(), MathUtils.getDirection(torigin), 10),
                            intersect3 = expanded.rayTrace(forigin.toVector(), MathUtils.getDirection(forigin), 10);

                    if(intersect != null || intersect2 != null || intersect3 != null)
                        hitboxHits++;
                }
            }

            hitboxes: {
                Hitboxes hitbox = find(Hitboxes.class);

                if(hitbox == null || !hitbox.isEnabled()) {
                    debug("Hitboxes is null within "
                            + data.getPlayer().getName() + " Reach (A) detection! (" + (hitbox == null) + ")");
                    break hitboxes;
                }

                if(hitboxHits == 0 && misses > 1) {
                    if(System.currentTimeMillis() - data.lagInfo.lastClientTrans < 150L
                            && data.lagInfo.lastPingDrop.isPassed(5) && ++hitbox.buffer > 5) {
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
                    if(transBetweenFlying > 1 && buffer > 2) {
                        cancelTicks = 10;
                        debug("Set to 10 cancel ticks");
                    } else if (++buffer > 6) {
                        buffer = 6;
                        vl++;
                        flag("distance=%.2f buffer=%s", distance, buffer);
                    }
                } else buffer -= buffer > 0 ? 0.05 : 0;
            } else buffer-= buffer > 0 ? 0.02 : 0;

            debug("distance=%.3f h/m=%s,%s boxes=%s buffer=%s hbhits=%s dt=%s lct=%s",
                    distance, hits, misses, targetLocs.size(), buffer, hitboxHits,
                    transBetweenFlying, System.currentTimeMillis() - data.lagInfo.lastClientTrans);
        }
        attacked = false;
    }

    private static SimpleCollisionBox getHitbox(Entity entity, KLocation loc) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc, entity);
    }
}
