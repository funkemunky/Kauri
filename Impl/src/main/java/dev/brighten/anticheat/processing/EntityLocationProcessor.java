package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.com.github.retrooper.packetevents.util.Vector3d;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.server.*;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityTeleportPacket;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class EntityLocationProcessor {

    private final ObjectData data;

    private final Map<UUID, EntityLocation> entityLocationMap = new ConcurrentHashMap<>();
    private final Timer lastFlying = new MillisTimer();
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
    void onRelPosition(WrapperPlayServerEntityRelativeMoveAndRotation packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getEntityId());

        if(!op.isPresent()) return;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        runAction(entity, () -> {
            //We don't need to do version checking here. Atlas handles this for us.
            eloc.newX += packet.getDeltaX();
            eloc.newY += packet.getDeltaY();
            eloc.newZ += packet.getDeltaZ();
            eloc.newYaw += packet.getYaw();
            eloc.newPitch += packet.getPitch();

            eloc.increment = 3;

            eloc.interpolatedLocations.clear();
        });
    }

    void onRelPosition(WrapperPlayServerEntityRelativeMove packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getEntityId());

        if(!op.isPresent()) return;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        runAction(entity, () -> {
            //We don't need to do version checking here. Atlas handles this for us.
            eloc.newX += packet.getDeltaX();
            eloc.newY += packet.getDeltaY();
            eloc.newZ += packet.getDeltaZ();

            eloc.increment = 3;

            eloc.interpolatedLocations.clear();
        });
    }

    void onRelPosition(WrapperPlayServerEntityRotation packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getEntityId());

        if(!op.isPresent()) return;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        runAction(entity, () -> {
            //We don't need to do version checking here. Atlas handles this for us.
            eloc.newYaw += packet.getYaw();
            eloc.newPitch += packet.getPitch();

            eloc.increment = 3;

            eloc.interpolatedLocations.clear();
        });
    }

    /**
     *
     * Processing PacketPlayOutEntityTeleport to update locations in a non-relative manner.
     *
     * @param packet WrappedOutEntityTeleportPacket
     */
    void onTeleportSent(WrapperPlayServerEntityTeleport packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getEntityId());

        if(!op.isPresent()) return;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        Vector3d vec = packet.getPosition();

        runAction(entity, () -> {
            if(data.playerVersion.isOrAbove(ProtocolVersion.V1_9)) {
                if (!(Math.abs(eloc.x - vec.getX()) >= 0.03125D)
                        && !(Math.abs(eloc.y - vec.getY()) >= 0.015625D)
                        && !(Math.abs(eloc.z - vec.getZ()) >= 0.03125D)) {
                    eloc.increment = 0;
                    //We don't need to do version checking here. Atlas handles this for us.
                    eloc.newX = eloc.x = vec.getX();
                    eloc.newY = eloc.y = vec.getY();
                    eloc.newZ = eloc.z = vec.getZ();
                    eloc.newYaw = eloc.yaw = packet.getYaw();
                    eloc.newPitch = eloc.pitch = packet.getPitch();
                    eloc.interpolatedLocations.clear();
                } else {
                    eloc.newX = vec.getX();
                    eloc.newY = vec.getY();
                    eloc.newZ = vec.getZ();
                    eloc.newYaw = packet.getYaw();
                    eloc.newPitch = packet.getPitch();

                    eloc.increment = 3;
                    eloc.interpolatedLocations.clear();
                }
            } else {
                //We don't need to do version checking here. Atlas handles this for us.
                eloc.newX = vec.getX();
                eloc.newY = vec.getY();
                eloc.newZ = vec.getZ();
                eloc.newYaw = packet.getYaw();
                eloc.newPitch = packet.getPitch();

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
            data.runInstantAction(ia -> {
                if(!ia.isEnd()) {
                    action.run();
                } else entityLocationMap.get(entity.getUniqueId()).oldLocations.clear();
            });
        } else {
            data.runKeepaliveAction(keepalive -> action.run());
            data.runKeepaliveAction(keepalive ->
                    entityLocationMap.get(entity.getUniqueId()).oldLocations.clear(), 1);
        }
    }
}
