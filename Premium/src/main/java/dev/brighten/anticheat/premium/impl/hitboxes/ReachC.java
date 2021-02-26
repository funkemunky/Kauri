package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityTeleportPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EntityLocation;
import dev.brighten.api.check.CheckType;
import lombok.RequiredArgsConstructor;
import org.bukkit.util.Vector;

import java.util.*;

@CheckInfo(name = "Reach (C)", description = "reach check what else do u need to know", checkType = CheckType.HITBOX,
        developer = true)
public class ReachC extends Check {
    private final Map<Integer, EntityTracker> trackerMap = new HashMap<>();
    private float buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.target == null || data.playerInfo.lastAttack.isPassed(0)) return;

        Optional<EntityTracker> tracker = Optional.ofNullable(trackerMap
                .getOrDefault(data.target.getEntityId(), null));

        if(!tracker.isPresent()) {
            debug("tracker was not present for entity id " + data.target.getEntityId());
            return;
        }

        tracker.get().getProperLocation(data.lagInfo.transPing + 2).ifPresent(locList -> {
            KLocation origin = data.playerInfo.to.clone();
            origin.y+= data.playerInfo.sneaking ? 1.54f : 1.62f;
            KLocation origin2 = data.playerInfo.from.clone();
            origin.y+= data.getPlayer().isSneaking() ? 1.54f : 1.62f;
            RayCollision ray = new RayCollision(origin.toVector(), MathUtils.getDirection(origin)),
                    ray2 = new RayCollision(origin2.toVector(), MathUtils.getDirection(origin2));
            double distance = 69.;
            int looped = 0;

            for (EntityLocation loc : locList) {
                for (int i = loc.interpolatedLocations.size() - 1 ; i >= 0 ; i--) {
                    KLocation iloc = loc.interpolatedLocations.get(i);
                    SimpleCollisionBox box = ((SimpleCollisionBox)EntityData.getEntityBox(iloc, data.target))
                            .expand(0.1);
                    Vector point = ray.collisionPoint(box), point2 = ray2.collisionPoint(box);

                    if(point == null && point2 == null) continue;

                    if(point != null)
                    distance = Math.min(origin.toVector().distance(point), distance);

                    if(point2 != null)
                        distance = Math.min(origin2.toVector().distance(point2), distance);

                    looped++;
                }
            }


            if(looped > 0 && distance > 3.0) {
                if(++buffer > 2) {
                    vl++;
                    flag("dist=%.3f", distance);
                }
            } else if(buffer > 0) buffer-= 0.05f;
            debug("(%s) dist=%.3f stamp=%s", looped, distance,
                    Kauri.INSTANCE.keepaliveProcessor.tick - data.lagInfo.transPing);
        });
    }

    @Packet
    public void onOut(WrappedOutRelativePosition packet) {
        if(packet.isPos()) {
            EntityTracker tracker = trackerMap.computeIfAbsent(packet.getId(), EntityTracker::new);

            EntityLocation loc = new EntityLocation(Kauri.INSTANCE.keepaliveProcessor.tick);
            loc.newX = loc.x = tracker.x;
            loc.newY = loc.y = tracker.y;
            loc.newZ = loc.z = tracker.z;
            if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_14)) {
                loc.newX+= (short)packet.getX() / 4096D;
                loc.newY+= (short)packet.getY() / 4096D;
                loc.newZ+= (short)packet.getZ() / 4096D;
            } else if(ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_9)) {
                loc.newX+= (int)packet.getX() / 4096D;
                loc.newY+= (int)packet.getY() / 4096D;
                loc.newZ+= (int)packet.getZ() / 4096D;
            } else {
                loc.newX+= (byte) packet.getX() / 32D;
                loc.newY+= (byte) packet.getY() / 32D;
                loc.newZ+= (byte) packet.getZ() / 32D;
            }

            loc.interpolateLocations();
            tracker.x = loc.x;
            tracker.y = loc.y;
            tracker.z = loc.z;

            tracker.locations.add(loc);
        }
    }

    @Packet
    public void onOut(WrappedOutEntityTeleportPacket packet) {
        EntityTracker tracker = trackerMap.computeIfAbsent(packet.entityId, EntityTracker::new);

        tracker.x = packet.x;
        tracker.y = packet.y;
        tracker.z = packet.z;

        EntityLocation loc = new EntityLocation(Kauri.INSTANCE.keepaliveProcessor.tick);
        loc.interpolatedLocations.add(new KLocation(tracker.x, tracker.y, tracker.z));
        tracker.locations.add(loc);
    }

    @RequiredArgsConstructor
    public static class EntityTracker {
        public final int id;
        public double x, y, z;
        public final List<EntityLocation> locations = Collections.synchronizedList(new EvictingList<>(15));

        public Optional<List<EntityLocation>> getProperLocation(int toGoBack) {
            synchronized (locations) {
                List<EntityLocation> locs = new ArrayList<>();
                for (int i = locations.size() - 1; i > 0; i--) {
                    EntityLocation loc = locations.get(i);

                    int stamp = Kauri.INSTANCE.keepaliveProcessor.tick - toGoBack;

                    if(loc.sentTick < stamp) {
                        locs.add(loc);
                        if(locs.size() >= 2) break;
                    }
                }
                if(locs.size() > 0) return Optional.of(locs);
            }
            return Optional.empty();
        }
    }
}
