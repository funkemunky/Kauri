package dev.brighten.anticheat.check.impl.premium.hitboxes;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityTeleportPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.RunUtils;
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
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.anticheat.utils.Helper;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.MillisTimer;
import dev.brighten.anticheat.utils.timer.impl.PlayerTimer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (B)", planVersion = KauriVersion.ARA, punishVL = 20, executable = true,
        checkType = CheckType.HITBOX)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private final Map<UUID, EntityLocation> entityLocationMap = new HashMap<>(),
            secondEntityLocationMap = new HashMap<>();
    private Timer lastFlying;
    public int streak;
    private float buffer;
    private int hbuffer;
    public boolean sentTeleport;
    private boolean attacked, flying;
    private AimG aimDetection;
    private KillauraH killauraHDetection;

    public Timer lastAimOnTarget = new TickTimer();
    private Timer lastTransProblem = new MillisTimer(20);
    private List<KLocation> targetLocs = new ArrayList<>();
    private int addTicks;

    private final boolean debugBoxes = true;

    private static final EnumSet<EntityType> allowedEntityTypes = EnumSet.of(EntityType.ZOMBIE, EntityType.SHEEP,
            EntityType.BLAZE, EntityType.SKELETON, EntityType.PLAYER, EntityType.VILLAGER, EntityType.IRON_GOLEM,
            EntityType.WITCH, EntityType.COW, EntityType.CREEPER);

    @Override
    public void setData(ObjectData data) {
        lastFlying = new PlayerTimer(data);
        super.setData(data);
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        if(data.target == null || !allowedEntityTypes.contains(data.target.getType())
                || packet.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
            return;

        attacked = true;
    }

    private AimG getAimDetection() {
        if(aimDetection == null) aimDetection = (AimG) data.checkManager.checks.get("Aim (G)");

        return aimDetection;
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flying = true;
        if(lastFlying.isNotPassed(1) && System.currentTimeMillis() - data.lagInfo.lastClientTrans < 65L) streak++;
        else {
            streak = 1;
            sentTeleport = false;
        }

        detection: {
            if(!attacked) break detection;

            attacked = false;

            //Updating new entity loc
            EntityLocation eloc = entityLocationMap.get(data.target.getUniqueId());

            if(eloc == null) {
                debug("eloc is null");
                break detection;
            }

            final KLocation to = data.playerInfo.to.clone(), from = data.playerInfo.from.clone();

            //debug("current loc: %.4f, %.4f, %.4f", eloc.x, eloc.y, eloc.z);

            to.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;
            from.y+= data.playerInfo.lsneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;
            if(eloc.x == 0 && eloc.y == 0 & eloc.z == 0) break detection;
            double distance = Double.MAX_VALUE;
            boolean collided = false; //Using this to compare smaller numbers than Double.MAX_VALUE. Slightly faster

            SimpleCollisionBox targetBox;

            if(eloc.oldLocation != null) {
                targetBox = Helper.wrap((SimpleCollisionBox) EntityData
                        .getEntityBox(new Vector(eloc.newX, eloc.newY, eloc.newZ), data.target),
                        (SimpleCollisionBox) EntityData.getEntityBox(eloc.oldLocation, data.target));
                debug("old location isnt null");
            } else {
                targetBox = Helper.wrap(eloc.interpolatedLocations.stream().map(l -> (SimpleCollisionBox) EntityData
                        .getEntityBox(l, data.target)).collect(Collectors.toList()));
            }

            if(targetBox == null) break detection;
            if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                targetBox = targetBox.expand(0.1, 0.1, 0.1);
            } else {
                targetBox = targetBox.expand(0.0325D);
            }

            final AxisAlignedBB vanillaBox = new AxisAlignedBB(targetBox);

            Vec3D intersectFrom = vanillaBox.rayTrace(from.toVector(), MathUtils.getDirection(from), 10);

            if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_9)) {
                Vec3D intersectTo = vanillaBox.rayTrace(to.toVector(), MathUtils.getDirection(to), 10);

                if(intersectTo != null) {
                    lastAimOnTarget.reset();
                    distance = Math.min(distance, intersectTo.distanceSquared(new Vec3D(to.x, to.y, to.z)));
                    collided = true;
                }
            }

            if(intersectFrom != null) {
                lastAimOnTarget.reset();
                distance = Math.min(distance, intersectFrom.distanceSquared(new Vec3D(from.x, from.y, from.z)));
                collided = true;
            }

            if(collided) {
                hbuffer = 0;
                distance = Math.sqrt(distance);
                if(distance > 3.02 && lastTransProblem.isPassed(52)) {
                    if(streak > 3 && sentTeleport) {
                        if(++buffer > 2) {
                            vl++;
                            flag("d=%.4f ltp=%s", distance, lastTransProblem.getPassed());
                            buffer = 2;
                        }
                    }
                } else if(buffer > 0) buffer-= 0.075f;
                debug("dist=%.2f b=%s s=%s st=%s lf=%s ld=%s lti=%s",
                        distance, buffer, streak, sentTeleport, lastFlying.getPassed(),
                        data.lagInfo.lastPingDrop.getPassed(), lastTransProblem.getPassed());
            } else {
                if(streak > 3 && sentTeleport) {
                    if (++hbuffer > 5) {
                        find(HitboxesB.class).vl++;
                        find(HitboxesB.class).flag(120, "%.1f;%.1f;%.1f", eloc.x, eloc.y, eloc.z);
                    }
                } else data.typesToCancel.add(CancelType.ATTACK);
                debug("didnt hit box: x=%.1f y=%.1f z=%.1f lti=%s", eloc.x, eloc.y, eloc.z,
                        lastTransProblem.getPassed());
            }
        }
        for (Iterator<Map.Entry<UUID, EntityLocation>> it = entityLocationMap.entrySet().iterator();
             it.hasNext();) {
            Map.Entry<UUID, EntityLocation> entry = it.next();

            EntityLocation eloc = entry.getValue();

            if(eloc.entity == null) {
                it.remove();
                continue;
            }

            if(eloc.increment == 0) continue;

            eloc.interpolateLocation();
        }


        lastFlying.reset();
    }

    @Packet
    public void onTrans(WrappedInTransactionPacket packet) {
        if(lastFlying.isPassed(1)
                && data.playerVersion.isOrAbove(ProtocolVersion.V1_9)
                && Kauri.INSTANCE.keepaliveProcessor.getKeepById(packet.getAction()).isPresent()) {

            for (Iterator<Map.Entry<UUID, EntityLocation>> it = entityLocationMap.entrySet().iterator();
                 it.hasNext();) {
                Map.Entry<UUID, EntityLocation> entry = it.next();

                EntityLocation eloc = entry.getValue();

                if(eloc.entity == null) {
                    it.remove();
                    continue;
                }

                if(eloc.increment == 0) continue;

                eloc.interpolateLocation();
            }
        }
    }

    private Map<Integer, List<KLocation>> resend = new HashMap<>();

    @Packet
    public boolean onEntity(WrappedOutRelativePosition packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.getId());

        if(!op.isPresent()) return false;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return false;

        List<KLocation> queuedForResend = resend.compute(entity.getEntityId(), (key, list) -> {
            if(list == null) return new ArrayList<>();

            return list;
        });


        double x,y,z;
        float yaw,pitch;

        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
            x = (byte)packet.getX() / 32D;
            y = (byte)packet.getY() / 32D;
            z = (byte)packet.getZ() / 32D;
            yaw = (float)(byte)packet.getYaw() / 256.0F * 360.0F;
            pitch = (float)(byte)packet.getPitch() / 256.0F * 360.0F;
        } else if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_14)) {
            x = (int)packet.getX() / 4096D;
            y = (int)packet.getY() / 4096D;
            z = (int)packet.getZ() / 4096D;
            yaw = (float)(byte)packet.getYaw() / 256.0F * 360.0F;
            pitch = (float)(byte)packet.getPitch() / 256.0F * 360.0F;
        } else {
            x = (short)packet.getX() / 4096D;
            y = (short)packet.getY() / 4096D;
            z = (short)packet.getZ() / 4096D;
            yaw = (float)(byte)packet.getYaw() / 256.0F * 360.0F;
            pitch = (float)(byte)packet.getPitch() / 256.0F * 360.0F;
        }

        KLocation toCompare = new KLocation(x, y, z, yaw, pitch);
        Optional<KLocation> needsToCancel = queuedForResend.stream()
                .filter(rp -> rp.toVector().distanceSquared(toCompare.toVector()) < 0.0001).findFirst();

        if(!needsToCancel.isPresent()) {
            EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                    key -> new EntityLocation(entity));

            queuedForResend.add(toCompare);
            resend.put(entity.getEntityId(), queuedForResend);
            WrappedOutRelativePosition newPacket = new WrappedOutRelativePosition(packet.getId(), x,
                    y, z, yaw, pitch, packet.isGround());
            RunUtils.task(() -> {
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

                    KillauraH detection = find(KillauraH.class);

                    detection.getTargetLocations().clear();
                    eloc.interpolatedLocations.clear();
                    eloc.interpolatedLocations.addAll(eloc.getInterpolatedLocations());
                    eloc.getInterpolatedLocations().stream()
                            .map(kloc -> {
                                SimpleCollisionBox box = (SimpleCollisionBox) EntityData.getEntityBox(kloc, entity);

                                if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                                    return box.expand(0.1);
                                }

                                return box;
                            }).forEach(detection.getTargetLocations()::add);

                    /*if(data.target != null && data.target.getEntityId() == packet.getId())
                    debug("Setting new posrot: %.4f, %.4f, %.4f, %s (%s)",
                            eloc.newX, eloc.newY, eloc.newZ, eloc.increment, System.currentTimeMillis());*/
                });
                TinyProtocolHandler.sendPacket(data.getPlayer(),
                        newPacket.getObject());
            });

            return true;
        } else {
            queuedForResend.remove(needsToCancel.get());
            resend.put(entity.getEntityId(), queuedForResend);
            return false;
        }
    }

    @Packet
    public boolean onTeleportSent(WrappedOutEntityTeleportPacket packet) {
        Optional<Entity> op = Atlas.getInstance().getWorldInfo(data.getPlayer().getWorld()).getEntity(packet.entityId);

        if(!op.isPresent()) return false;

        Entity entity = op.get();

        if(!allowedEntityTypes.contains(entity.getType())) return false;

        EntityLocation eloc = entityLocationMap.computeIfAbsent(entity.getUniqueId(),
                key -> new EntityLocation(entity));

        List<KLocation> queuedForResend = resend.compute(entity.getEntityId(), (key, list) -> {
            if(list == null) return new ArrayList<>();

            return list;
        });
        KLocation toCompare = new KLocation(packet.x, packet.y, packet.z, packet.yaw, packet.pitch);
        Optional<KLocation> needsToCancel = queuedForResend.stream()
                .filter(rp -> rp.toVector().distanceSquared(toCompare.toVector()) < 0.0001).findFirst();

        if(!needsToCancel.isPresent()) {

            queuedForResend.add(toCompare);
            resend.put(entity.getEntityId(), queuedForResend);
            WrappedOutEntityTeleportPacket newPacket = new WrappedOutEntityTeleportPacket(packet.entityId,
                    packet.x, packet.y, packet.z, packet.yaw, packet.pitch, packet.onGround);

            RunUtils.task(() -> {
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
                            eloc.interpolatedLocations.add(new KLocation(eloc.x, eloc.y, eloc.z, eloc.yaw, eloc.pitch));
                        } else {
                            eloc.newX = packet.x;
                            eloc.newY = packet.y;
                            eloc.newZ = packet.z;
                            eloc.newYaw = packet.yaw;
                            eloc.newPitch = packet.pitch;

                            eloc.increment = 3;
                            eloc.interpolatedLocations.clear();
                            eloc.interpolatedLocations.addAll(eloc.getInterpolatedLocations());
                        }
                    } else {
                        //We don't need to do version checking here. Atlas handles this for us.
                        eloc.newX = packet.x;
                        eloc.newY = packet.y;
                        eloc.newZ = packet.z;
                        eloc.newYaw = packet.yaw;
                        eloc.newPitch = packet.pitch;

                        eloc.increment = 3;
                        eloc.interpolatedLocations.clear();
                        eloc.interpolatedLocations.addAll(eloc.getInterpolatedLocations());
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

                    /*if(data.target != null && data.target.getEntityId() == packet.entityId)
                    debug("Setting new posrot: %.4f, %.4f, %.4f, %s (%s)",
                            eloc.newX, eloc.newY, eloc.newZ, eloc.increment, System.currentTimeMillis());*/

                    sentTeleport = eloc.sentTeleport = true;
                });
                TinyProtocolHandler.sendPacket(data.getPlayer(), newPacket.getObject());
            });
            return true;
        } else {
            queuedForResend.remove(needsToCancel.get());
            resend.put(entity.getEntityId(), queuedForResend);
        }
        return false;
    }

    private void runAction(Entity entity, Runnable action) {
        if(data.target != null && data.target.getUniqueId().equals(entity.getUniqueId())) {
            AtomicLong start = new AtomicLong();
            data.runInstantAction(ia -> {
                if(!ia.isEnd()) {
                    flying = false;
                    action.run();
                    start.set(System.currentTimeMillis());
                } else {
                    entityLocationMap.computeIfPresent(entity.getUniqueId(), (key, eloc) -> {
                        eloc.oldLocation = null;
                        return eloc;
                    });
                    if(flying || System.currentTimeMillis() - start.get() > 4) {
                        sentTeleport = false;
                        lastTransProblem.reset();
                    }
                }

                if(!ia.isEnd()) {
                }
            }, true);
        } else data.runKeepaliveAction(keepalive -> action.run());
    }

}
