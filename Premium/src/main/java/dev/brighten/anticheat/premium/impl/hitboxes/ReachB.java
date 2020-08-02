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
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (B)", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 8)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private long lastUse;
    private float buffer;
    private Entity entity;

    @Setting(name = "debug")
    private static boolean debug = false;

    @Packet
    public void onFly(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastUse == 0 && entity != null) {
            if(data.playerInfo.creative) return;

            List<CollisionBox> entityLocs = data.targetPastLocation.getEstimatedLocation(timeStamp,
                            data.lagInfo.transPing, 200L)
                    .stream()
                    .map(loc -> getHitbox(entity, loc)).collect(Collectors.toList());

            List<SimpleCollisionBox> simpleBoxes = new ArrayList<>();

            entityLocs.forEach(box -> box.downCast(simpleBoxes));

            entityLocs.clear();

            double distance = 69, horzDistance = 69;
            int misses = 0, collided = 0;
            for (KLocation originLoc : Arrays.asList(data.playerInfo.to.clone(), data.playerInfo.from.clone())) {
                originLoc.y+= data.playerInfo.sneaking ? 1.54 : 1.62;
                RayCollision ray = new RayCollision(originLoc.toVector(), MathUtils.getDirection(originLoc));
                if(debug) ray.draw(WrappedEnumParticle.CRIT, Bukkit.getOnlinePlayers());
                for (SimpleCollisionBox sbox : simpleBoxes) {
                    SimpleCollisionBox box = sbox.copy();
                    box.expand(0.1);
                    if(debug) box.draw(WrappedEnumParticle.FLAME, Bukkit.getOnlinePlayers());
                    val check = RayCollision.distance(ray, box);

                    horzDistance = Math.min(horzDistance, box.max().midpoint(box.min()).setY(0)
                            .distance(originLoc.toVector().setY(0)) - .4);
                    if(check == -1) {
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

            if(collided > 1 && data.lagInfo.lastPacketDrop.hasPassed(2)) {
                if(distance > 3.02 &&
                        Kauri.INSTANCE.lastTickLag.hasPassed(40)) {
                    if(++buffer > 4) {
                        vl++;
                        flag("distance=%v.3 buffer=%v.1 misses=%v", distance, buffer, misses);
                    }
                } else buffer-= buffer > 0 ? data.playerVersion.isAbove(ProtocolVersion.V1_8_9) ? 0.1f : 0.05f : 0;
            }

            debug("distance=%v.3 hdist=%v.3 buffer=%v.2 ticklag=%v collided=%v delta=%v",
                    distance, horzDistance, buffer, Kauri.INSTANCE.lastTickLag.getPassed(), collided,
                    timeStamp - lastUse);
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        lastUse = timeStamp;
        entity = packet.getEntity();
    }

    @Packet
    public void onArm9(WrappedInArmAnimationPacket packet) {
        buffer-= buffer > 0 ? 0.01 : 0;
    }

    private static CollisionBox getHitbox(Entity entity, KLocation loc) {
        return EntityData.getEntityBox(loc, entity);
    }
}