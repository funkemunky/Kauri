package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityTeleportPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class EntityLocationProcessor {

    private final ObjectData data;

    private final Map<UUID, EntityLocation> entityLocationMap = new ConcurrentHashMap<>();
    public final Timer lastFlying = new MillisTimer(), lastProblem = new TickTimer();
    public int streak;

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
            EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    /**
     *
     * Returns the EntityLocation based on the provided Entity's UUID. May be null if the Entity is not
     * being tracked, so we use an Optional since it could be non existent.
     *
     * @param entity Entity
     * @return Optional<EntityLocation></EntityLocation>
     */
    public Optional<EntityLocation> getEntityLocation(Entity entity) {
        return Optional.ofNullable(entityLocationMap.get(entity.getUniqueId()));
    }

    /**
     *
     * We are processing PacketPlayInFlying to iterate the tracked entity locations
     *
     */
    void onFlying() {
        if(lastFlying.isNotPassed(1)) streak++;
        else {
            streak = 1;
        }

        entityLocationMap.values().forEach(EntityLocation::interpolateLocation);
        lastFlying.reset();
    }

    /**
     *
     * Processing PacketPlayOutRelativePosition for updating entity locations in a relative manner.
     *
     * @param packet WrappedOutRelativePosition
     */
    void onRelPosition(WrappedOutRelativePosition packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getId());

        if(!op.isPresent()) return;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        runAction(entity, () -> {
            //We don't need to do version checking here. Atlas handles this for us.
            if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
                eloc.newX += (byte)packet.getX() / 32D;
                eloc.newY += (byte)packet.getY() / 32D;
                eloc.newZ += (byte)packet.getZ() / 32D;
                eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
            } else if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_14)) {
                eloc.newX += (int)packet.getX() / 4096D;
                eloc.newY += (int)packet.getY() / 4096D;
                eloc.newZ += (int)packet.getZ() / 4096D;
                eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
            } else {
                eloc.newX += (short)packet.getX() / 4096D;
                eloc.newY += (short)packet.getY() / 4096D;
                eloc.newZ += (short)packet.getZ() / 4096D;
                eloc.newYaw += (float)(byte)packet.getYaw() / 256.0F * 360.0F;
                eloc.newPitch += (float)(byte)packet.getPitch() / 256.0F * 360.0F;
            }

            eloc.increment = 3;
        });
    }

    /**
     *
     * Processing PacketPlayOutEntityTeleport to update locations in a non-relative manner.
     *
     * @param packet WrappedOutEntityTeleportPacket
     */
    void onTeleportSent(WrappedOutEntityTeleportPacket packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.entityId);

        if(!op.isPresent()) return;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        runAction(entity, () -> {
            if(data.playerVersion.isOrAbove(ProtocolVersion.V1_9)) {
                if (!(Math.abs(eloc.x - packet.x) >= 0.03125D)
                        && !(Math.abs(eloc.y - packet.y) >= 0.015625D)
                        && !(Math.abs(eloc.z - packet.z) >= 0.03125D)) {
                    eloc.increment = 0;
                    //We don't need to do version checking here. Atlas handles this for us.
                    eloc.newX = eloc.x = packet.x;
                    eloc.newY = eloc.y = packet.y;
                    eloc.newZ = eloc.z = packet.z;
                    eloc.newYaw = eloc.yaw = packet.yaw;
                    eloc.newPitch = eloc.pitch = packet.pitch;
                } else {
                    eloc.newX = packet.x;
                    eloc.newY = packet.y;
                    eloc.newZ = packet.z;
                    eloc.newYaw = packet.yaw;
                    eloc.newPitch = packet.pitch;

                    eloc.increment = 3;
                }
            } else {
                //We don't need to do version checking here. Atlas handles this for us.
                eloc.newX = packet.x;
                eloc.newY = packet.y;
                eloc.newZ = packet.z;
                eloc.newYaw = packet.yaw;
                eloc.newPitch = packet.pitch;

                eloc.increment = 3;
            }
        });
    }

    /**
     *
     * We are running an action when a transaction is received. If the Entity provided is currently a target,
     * we want to send a tranasction on this method being run and use that to more accurately get an estimate of when
     * the client receives the transaction relative to what we want in the action. If not the target, then we use our
     * transaction system which sends one transaction every tick, and then on return runs a list of Runnables which
     * may be less accurate in some situations, but uses less processing and network resources.
     *
     * @param entity Entity
     * @param action Runnable
     */
    private void runAction(Entity entity, Runnable action) {
        if(data.target != null && data.target.getEntityId() == entity.getEntityId()) {
            AtomicLong start = new AtomicLong();
            data.runInstantAction(ia -> {
                if(!ia.isEnd()) {
                    long delta = System.currentTimeMillis() - start.get();

                    if(delta > 10) {
                        lastProblem.reset();
                    }
                    action.run();
                } else {
                    entityLocationMap.get(entity.getUniqueId()).oldLocations.clear();
                    start.set(System.currentTimeMillis());
                }
            });
        } else {
            data.runKeepaliveAction(keepalive -> action.run());
            data.runKeepaliveAction(keepalive ->
                    entityLocationMap.get(entity.getUniqueId()).oldLocations.clear(), 1);
        }
    }
}
