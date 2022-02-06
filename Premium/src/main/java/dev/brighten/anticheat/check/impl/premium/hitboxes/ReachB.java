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
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import dev.brighten.api.check.DevStage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    public void onFlying(WrappedInUseEntityPacket packet) {
        flying = true;

        detection: {
            if(!attacked) break detection;

            attacked = false;

            //Updating new entity loc
            EntityLocation eloc = entityLocationMap.get(data.target.getUniqueId());

            if(eloc == null) {
                debug("eloc is null");
                break detection;
            }

            if(data.playerInfo.inVehicle) break detection;

            final KLocation to = data.playerInfo.to.clone();

            //debug("current loc: %.4f, %.4f, %.4f", eloc.x, eloc.y, eloc.z);

            to.y+= data.playerInfo.sneaking ? (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)
                    ? 1.27f : 1.54f) : 1.62f;
            if(eloc.x == 0 && eloc.y == 0 & eloc.z == 0) break detection;
            double distance = Double.MAX_VALUE;
            boolean collided = false; //Using this to compare smaller numbers than Double.MAX_VALUE. Slightly faster

            SimpleCollisionBox targetBox = null;

            List<SimpleCollisionBox> boxes = new ArrayList<>();
            if(eloc.oldLocations.size() > 0) {
                for (KLocation oldLocation : eloc.oldLocations) {
                    SimpleCollisionBox box = (SimpleCollisionBox)
                            EntityData.getEntityBox(oldLocation.toVector(), data.target);

                    if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                        box = box.expand(0.1, 0.1, 0.1);
                    }
                    boxes.add(box);
                }
                for (KLocation oldLocation : eloc.interpolatedLocations) {
                    SimpleCollisionBox box = (SimpleCollisionBox)
                            EntityData.getEntityBox(oldLocation.toVector(), data.target);

                    if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                        box = box.expand(0.1, 0.1, 0.1);
                    }
                    boxes.add(box);
                }
            } else {
                for (KLocation oldLocation : eloc.interpolatedLocations) {
                    SimpleCollisionBox box = (SimpleCollisionBox)
                            EntityData.getEntityBox(oldLocation.toVector(), data.target);

                    if(data.playerVersion.isBelow(ProtocolVersion.V1_9)) {
                        box = box.expand(0.1, 0.1, 0.1);
                    }
                    boxes.add(box);
                }
                debug("old location is null");
            }

            if(boxes.size() > 0)
            targetBox = Helper.wrap(boxes);

            if(targetBox == null) break detection;

            if(data.playerVersion.isOrAbove(ProtocolVersion.V1_9))
            targetBox = targetBox.expand(0.0325D);

            final AxisAlignedBB vanillaBox = new AxisAlignedBB(targetBox);

            Vec3D intersectTo = vanillaBox.rayTrace(to.toVector(), MathUtils.getDirection(to), 10);

            if(intersectTo != null) {
                lastAimOnTarget.reset();
                distance = Math.min(distance, intersectTo.distanceSquared(new Vec3D(to.x, to.y, to.z)));
                collided = true;
            }

            if(collided && eloc.oldLocations.size() > 0) {
                hbuffer = 0;
                distance = Math.sqrt(distance);
                final double threshold = lastTransProblem.isNotPassed(50) ? 3.5 : 3.02;
                if(distance > threshold) {
                    if(++buffer > 3) {
                        vl++;
                        flag("d=%.3f>-%.2f ltp=%s", distance, threshold, lastTransProblem.getPassed());
                        buffer = 3;
                    }
                } else if(buffer > 0) buffer-= 0.075f;
                debug("dist=%.2f>-%.2f b=%s s=%s st=%s lf=%s ld=%s lti=%s",
                        distance, threshold, buffer, streak, sentTeleport, lastFlying.getPassed(),
                        data.lagInfo.lastPingDrop.getPassed(), lastTransProblem.getPassed());
            } else {
                if(streak > 3 && sentTeleport) {
                    if (++hbuffer > 5) {
                        find(HitboxesB.class).vl++;
                        find(HitboxesB.class).flag(120, "%.1f;%.1f;%.1f", eloc.x, eloc.y, eloc.z);
                    }
                }
                debug("didnt hit box: x=%.1f y=%.1f z=%.1f lti=%s", eloc.x, eloc.y, eloc.z,
                        lastTransProblem.getPassed());
            }
        }


        lastFlying.reset();
    }

    private final Map<Integer, List<KLocation>> resend = new Int2ObjectOpenHashMap<>();

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

            eloc.oldLocations.clear();
            eloc.oldLocations.addAll(eloc.interpolatedLocations);

            KillauraH detection = find(KillauraH.class);

            detection.getTargetLocations().clear();
            eloc.interpolateLocations();
            eloc.interpolatedLocations.stream()
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

                    /*if(data.target != null && data.target.getEntityId() == packet.getId())
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
                    eloc.oldLocations.clear();
                    eloc.oldLocations.addAll(eloc.interpolatedLocations);
                    eloc.interpolateLocations();
                } else {
                    eloc.newX = packet.x;
                    eloc.newY = packet.y;
                    eloc.newZ = packet.z;
                    eloc.newYaw = packet.yaw;
                    eloc.newPitch = packet.pitch;

                    eloc.increment = 3;
                    eloc.oldLocations.clear();
                    eloc.oldLocations.addAll(eloc.interpolatedLocations);
                    eloc.interpolateLocations();
                }
            } else {
                //We don't need to do version checking here. Atlas handles this for us.
                eloc.newX = packet.x;
                eloc.newY = packet.y;
                eloc.newZ = packet.z;
                eloc.newYaw = packet.yaw;
                eloc.newPitch = packet.pitch;

                eloc.increment = 3;
                eloc.oldLocations.clear();
                eloc.oldLocations.addAll(eloc.interpolatedLocations);
                eloc.interpolateLocations();
            }

            KillauraH detection = find(KillauraH.class);
            detection.getTargetLocations().clear();
            eloc.interpolatedLocations.stream()
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

                    /*if(data.target != null && data.target.getEntityId() == packet.entityId)
                    debug("Setting new posrot: %.4f, %.4f, %.4f, %s (%s)",
                            eloc.newX, eloc.newY, eloc.newZ, eloc.increment, System.currentTimeMillis());*/

            sentTeleport = eloc.sentTeleport = true;
        });
        return false;
    }

    private void runAction(Entity entity, Runnable action) {
        if(data.target != null && data.target.getUniqueId().equals(entity.getUniqueId())) {
            AtomicLong start = new AtomicLong();
            data.runInstantAction(ia -> {
                if(!ia.isEnd()) {
                    flying = false;
                    start.set(System.currentTimeMillis());
                } else {
                    action.run();
                    long delta = System.currentTimeMillis() - start.get();
                    if(delta > 4) {
                        lastTransProblem.reset();
                    }
                }
            }, true);
        } else data.runKeepaliveAction(keepalive -> action.run());
    }

}
