package dev.brighten.anticheat.check.impl.premium.hitboxes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityTeleportPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.impl.premium.AimG;
import dev.brighten.anticheat.check.impl.premium.KillauraH;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.check.impl.premium.util.EntityLocation;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.anticheat.utils.timer.impl.PlayerTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@CheckInfo(name = "Reach (B)", planVersion = KauriVersion.ARA, punishVL = 6, executable = true,
        checkType = CheckType.HITBOX)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    public final Map<UUID, EntityLocation> entityLocationMap = new ConcurrentHashMap<>();
    private Timer lastFlying;
    public int streak;
    private float buffer;
    private int hbuffer;
    public boolean sentTeleport;
    private AimG aimDetection;

    public Timer lastAimOnTarget = new TickTimer();
    private final Timer lastTransProblem = new MillisTimer(20);
    private final Queue<Entity> attacks = new LinkedBlockingQueue<>();

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
            EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Override
    public void setData(ObjectData data) {
        lastFlying = new PlayerTimer(data);
        super.setData(data);
    }

    private AimG getAimDetection() {
        if(aimDetection == null) aimDetection = (AimG) data.checkManager.checks.get("Aim (G)");

        return aimDetection;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(packet.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                && allowedEntityTypes.contains(packet.getEntity().getType())) {
            attacks.add(packet.getEntity());
        }
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(lastFlying.isNotPassed(1)) streak++;
        else {
            streak = 1;
            sentTeleport = false;
        }

        entityLocationMap.values().forEach(EntityLocation::interpolateLocation);
        
        detection: {
            if(data.playerInfo.creative || data.playerInfo.inVehicle) {
                attacks.clear();
                debug("creative");
                break detection;
            }
            Entity target;
            
            while((target = attacks.poll()) != null) {
                //Updating new entity loc
                EntityLocation eloc = entityLocationMap.get(target.getUniqueId());

                if(eloc == null) {
                    debug("eloc is null");
                    break detection;
                }

                final KLocation to = data.playerInfo.to.clone(), from = data.playerInfo.from.clone();

                //debug("current loc: %.4f, %.4f, %.4f", eloc.x, eloc.y, eloc.z);

                to.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                        ? 1.27f : 1.54f) : 1.62f;
                from.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                        ? 1.27f : 1.54f) : 1.62f;
                
                if(eloc.x == 0 && eloc.y == 0 & eloc.z == 0) {
                    debug("eloc is all 0 wtf");
                    break detection;
                }
                double distance = Double.MAX_VALUE;
                boolean collided = false; //Using this to compare smaller numbers than Double.MAX_VALUE. Slightly faster

                List<SimpleCollisionBox> boxes = new ArrayList<>();
                if(eloc.oldLocations.size() > 0) {
                    for (KLocation oldLocation : eloc.oldLocations) {
                        SimpleCollisionBox box = (SimpleCollisionBox)
                                EntityData.getEntityBox(oldLocation.toVector(), target);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            box = box.expand(0.1);
                        } else box = box.expand(0.0325);
                        boxes.add(box);
                    }
                    for (KLocation oldLocation : eloc.interpolatedLocations) {
                        SimpleCollisionBox box = (SimpleCollisionBox)
                                EntityData.getEntityBox(oldLocation.toVector(), target);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            box = box.expand(0.1);
                        } else box = box.expand(0.0325);
                        boxes.add(box);
                    }
                } else {
                    for (KLocation oldLocation : eloc.interpolatedLocations) {
                        SimpleCollisionBox box = (SimpleCollisionBox)
                                EntityData.getEntityBox(oldLocation.toVector(), target);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            box = box.expand(0.1);
                        } else box = box.expand(0.0325);
                        boxes.add(box);
                    }
                }

                if(boxes.size() == 0) break detection;

                int hits = 0;

                for (SimpleCollisionBox targetBox : boxes) {
                    final AxisAlignedBB vanillaBox = new AxisAlignedBB(targetBox);

                    Vec3D intersectTo = vanillaBox.rayTrace(to.toVector(), MathUtils.getDirection(to), 10),
                            intersectFrom = vanillaBox.rayTrace(from.toVector(),
                                    MathUtils.getDirection(from), 10);

                    if(intersectTo != null) {
                        lastAimOnTarget.reset();
                        hits++;
                        distance = Math.min(distance, intersectTo.distanceSquared(new Vec3D(to.x, to.y, to.z)));
                        collided = true;
                    }
                    if(intersectFrom != null) {
                        lastAimOnTarget.reset();
                        hits++;
                        distance = Math.min(distance, intersectFrom.distanceSquared(new Vec3D(to.x, to.y, to.z)));
                        collided = true;
                    }
                }

                if(collided) {
                    hbuffer = 0;
                    distance = Math.sqrt(distance);
                    if(distance > 3.05) {
                        if(++buffer > 2) {
                            vl++;
                            flag("d=%.3f>-3.05 ltp=%s", distance, lastTransProblem.getPassed());
                            buffer = Math.min(2, buffer);
                        }
                    } else if(buffer > 0) buffer-= 0.05f;
                    debug((distance > 3.001 ? Color.Green : "")
                                    +"dist=%.2f>-3.001 hits-%s b=%s s=%s st=%s lf=%s ld=%s lti=%s",
                            distance, hits, buffer, streak, sentTeleport, lastFlying.getPassed(),
                            data.lagInfo.lastPingDrop.getPassed(), lastTransProblem.getPassed());
                } else {
                    if (++hbuffer > 5) {
                        find(HitboxesB.class).vl++;
                        find(HitboxesB.class).flag(120, "%.1f;%.1f;%.1f", eloc.x, eloc.y, eloc.z);
                    }
                    debug("didnt hit box: x=%.1f y=%.1f z=%.1f lti=%s", eloc.x, eloc.y, eloc.z,
                            lastTransProblem.getPassed());
                }
            }
        }


        lastFlying.reset();
    }

    @Packet
    public boolean onEntity(WrappedOutRelativePosition packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getId());

        if(!op.isPresent()) return false;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return false;

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

            eloc.interpolatedLocations.clear();

            KillauraH detection = find(KillauraH.class);

            detection.getTargetLocations().clear();
            eloc.getInterpolatedLocations().stream()
                    .map(kloc -> {
                        SimpleCollisionBox box = (SimpleCollisionBox) EntityData.getEntityBox(kloc, entity);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            return box.expand(0.1);
                        }
                        return box;
                    }).forEach(detection.getTargetLocations()::add);
            eloc.oldLocations.stream()
                    .map(kloc -> {
                        SimpleCollisionBox box = (SimpleCollisionBox) EntityData.getEntityBox(kloc, entity);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            return box.expand(0.1);
                        }
                        return box;
                    }).forEach(detection.getTargetLocations()::add);

                    /*if(target != null && target.getEntityId() == packet.getId())
                    debug("Setting new posrot: %.4f, %.4f, %.4f, %s (%s)",
                            eloc.newX, eloc.newY, eloc.newZ, eloc.increment, System.currentTimeMillis());*/
        });
        return false;
    }

    @Packet
    public boolean onTeleportSent(WrappedOutEntityTeleportPacket packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.entityId);

        if(!op.isPresent()) return false;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return false;

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
                    eloc.interpolatedLocations.clear();
                } else {
                    eloc.newX = packet.x;
                    eloc.newY = packet.y;
                    eloc.newZ = packet.z;
                    eloc.newYaw = packet.yaw;
                    eloc.newPitch = packet.pitch;

                    eloc.increment = 3;
                    eloc.interpolatedLocations.clear();
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

            KillauraH detection = find(KillauraH.class);
            detection.getTargetLocations().clear();
            eloc.getInterpolatedLocations().stream()
                    .map(kloc -> {
                        SimpleCollisionBox box = (SimpleCollisionBox) EntityData.getEntityBox(kloc, entity);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            return box.expand(0.1);
                        }

                        return box;
                    }).forEach(detection.getTargetLocations()::add);
            eloc.oldLocations.stream()
                    .map(kloc -> {
                        SimpleCollisionBox box = (SimpleCollisionBox) EntityData.getEntityBox(kloc, entity);

                        if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                            return box.expand(0.1);
                        }
                        return box;
                    }).forEach(detection.getTargetLocations()::add);

                    /*if(target != null && target.getEntityId() == packet.entityId)
                    debug("Setting new posrot: %.4f, %.4f, %.4f, %s (%s)",
                            eloc.newX, eloc.newY, eloc.newZ, eloc.increment, System.currentTimeMillis());*/

            sentTeleport = eloc.sentTeleport = true;
        });
        return false;
    }

    private void runAction(Entity entity, Runnable action) {
        if(data.target != null && data.target.getEntityId() == entity.getEntityId()) {
            data.runInstantAction(ia -> {
                if(!ia.isEnd()) {
                    action.run();
                } else entityLocationMap.get(entity.getUniqueId()).oldLocations.clear();
            });
        } else {
            data.runKeepaliveAction(keepalive -> action.run());
            data.runKeepaliveAction(keepalive -> {
                entityLocationMap.get(entity.getUniqueId()).oldLocations.clear();
            }, 1);
        }
    }

}
