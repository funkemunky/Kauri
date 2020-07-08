package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Reach (B)", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 8)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private long lastUse;
    private float buffer;
    private LivingEntity target;

    @Setting(name = "debug")
    private static boolean debug = false;

    @Packet
    public void onFly(WrappedInUseEntityPacket packet) {
        if(packet.getEntity() instanceof LivingEntity
                && packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)) {
            if(data.playerInfo.creative || data.targetLoc == null) return;
            target = (LivingEntity) packet.getEntity();

            long amount = data.lagInfo.millisPing % 50;

            List<KLocation> locs = new ArrayList<>();

            if(data.targetPastLocation.previousLocations.size() < 15) return;

            double inter = 1 - (amount / 50D);
            int ticks = 0;
            for (int i = data.targetPastLocation.previousLocations.size(); i > 0; i--) {
                KLocation loc = data.targetPastLocation.previousLocations.get(i - 1).clone();

                if(i > 1) {
                    KLocation fromLoc = data.targetPastLocation.previousLocations.get(i - 2).clone();

                    Vector interpolate = loc.toVector().subtract(fromLoc.toVector());

                    interpolate.multiply(inter);

                    fromLoc.x += interpolate.getX();
                    fromLoc.y += interpolate.getY();
                    fromLoc.z += interpolate.getZ();

                    if(i > 14) {
                        //debug("i=%v time:%v now:%v", i, loc.timeStamp, current);
                        //debug("(int) %v (%v): x=%v.5 y=%v.5 z=%v.5", current - (int) loc.timeStamp,
                        //        data.lagInfo.ping, fromLoc.x, fromLoc.y, fromLoc.z);
                        //debug("%v (%v): x=%v.5 y=%v.5 z=%v.5", current - (int) loc.timeStamp,
                        //        data.lagInfo.ping, loc.x, loc.y, loc.z);
                    }
                    locs.add(fromLoc);

                    if(ticks++ > data.lagInfo.ping + 7) break;
                }
            }

            int ping = Math.min(locs.size() - 1, data.lagInfo.ping + 4);
            List<CollisionBox> entityLocs = Arrays.asList(data.targetLoc, locs.get(ping)).stream()
                    .map(loc -> getHitbox(data.getPlayer(), loc))
                    .collect(Collectors.toList());

            List<SimpleCollisionBox> simpleBoxes = new ArrayList<>();

            entityLocs.forEach(box -> box.downCast(simpleBoxes));
            entityLocs.clear();

            double distance = 69, horzDistance = 69;
            int misses = 0, collided = 0;

            KLocation from = data.playerInfo.from.clone(), to = data.playerInfo.to.clone();

            Vector xInt = to.toVector().subtract(from.toVector());

            xInt.multiply(inter);

            from.x+= xInt.getX();
            from.y+= xInt.getY();
            from.z+= xInt.getZ();

            from.yaw+= data.playerInfo.deltaYaw * inter;
            from.pitch+= data.playerInfo.deltaPitch * inter;

            //We use the player object since this will basically be the from loc anyway.
            from.y+= data.getPlayer().isSneaking() ? 1.52f : 1.64f;
            to.y+= data.playerInfo.sneaking ? 1.52f : 1.64f;

            List<KLocation> origins = Arrays.asList(from, to);

            for (KLocation origin : origins) {
                RayCollision ray = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));
                if(debug) ray.draw(WrappedEnumParticle.CRIT, Bukkit.getOnlinePlayers());
                for (SimpleCollisionBox sbox : simpleBoxes) {
                    SimpleCollisionBox box = sbox.copy();
                    box.expand(0.1);
                    if (debug) box.draw(WrappedEnumParticle.CRIT_MAGIC, Bukkit.getOnlinePlayers());
                    val check = RayCollision.distance(ray, box);

                    horzDistance = Math.min(horzDistance, box.max().midpoint(box.min()).setY(0)
                            .distance(from.toVector().setY(0)) - .4);
                    if (check == -1) {
                        misses++;
                        continue;
                    } else collided++;
                    distance = Math.min(distance, check);
                }
            }

            if(distance == 69) {
                buffer-= buffer > 0 ? 0.01f : 0;
                debug("none collided: " + misses + ", " + entityLocs.size());
                return;
            }

            if(distance > 3.01 && collided > 2 &&
                    data.lagInfo.lastPacketDrop.hasPassed(2) &&
                    Kauri.INSTANCE.lastTickLag.hasPassed(40)) {
                if(++buffer > 4) {
                    vl++;
                    flag("distance=%v.3 buffer=%v.1 misses=%v", distance, buffer, misses);
                }
            } else buffer-= buffer > 0 ? data.playerVersion.isAbove(ProtocolVersion.V1_8_9) ? 0.25f : 0.1f : 0;

            debug("distance=%v.3 hdist=%v.3 buffer=%v.2 ticklag=%v collided=%v ping=%v interpolated=%v.2%",
                    distance, horzDistance, buffer, Kauri.INSTANCE.lastTickLag.getPassed(), collided,
                    data.lagInfo.millisPing, inter * 100);
        }
    }

    private static CollisionBox getHitbox(Entity entity, KLocation loc) {
        return EntityData.getEntityBox(loc, entity);
    }
}