package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.world.CollisionBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.AxisAlignedBB;
import dev.brighten.anticheat.utils.Vec3D;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.db.utils.Pair;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (B)", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 8, planVersion = KauriVersion.ARA)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private float buffer;

    @Setting(name = "debug")
    private static boolean debug = false;

    @Packet
    public void onFly(WrappedInFlyingPacket packet, long timeStamp) {
        if(data.playerInfo.lastAttack.isNotPassed(0) && data.target != null) {
            if(data.playerInfo.creative) return;

            List<Pair<SimpleCollisionBox, Double>> entityLocs = data.targetPastLocation.getEstimatedLocation(timeStamp,
                    (data.lagInfo.transPing + 2) * 50, 100L)
                    .stream()
                    .map(loc -> {
                        SimpleCollisionBox hitbox = (SimpleCollisionBox) getHitbox(data.target, loc);
                        int index = data.targetPastLocation.getPreviousLocations().indexOf(loc);
                        return new Pair<>(hitbox, Math.max(0,index > 0 ? data.targetPastLocation.getPreviousLocations()
                                .get(index- 1)
                                .toVector().distance(loc.toVector()) : 0));
                    }).collect(Collectors.toList());

            double distance = 69, fdistance = 69, tdistance = 69;
            int misses = 0, collided = 0, fmisses = 0, tmisses = 0, fcollided = 0, tcollided = 0;
            Location toOrigin = data.playerInfo.to.toLocation(data.getPlayer().getWorld()),
                    fromOrigin = data.getPlayer().getEyeLocation();

            toOrigin.setY(toOrigin.getY() + (data.playerInfo.sneaking ? 1.54 : 1.62));
            for (Pair<SimpleCollisionBox, Double> sbox : entityLocs) {
                val copied = sbox.key.copy().expand(0.1);
                AxisAlignedBB aabb = new AxisAlignedBB(copied);
                if(debug) copied.draw(WrappedEnumParticle.FLAME, Bukkit.getOnlinePlayers());
                Vec3D checkTo = aabb.rayTrace(toOrigin.toVector(), toOrigin.getDirection(), 10),
                        checkFrom = aabb.rayTrace(fromOrigin.toVector(), fromOrigin.getDirection(), 10);

                if(checkTo != null) {
                    tdistance = Math.min(new Vector(checkTo.x, checkTo.y, checkTo.z)
                            .distance(toOrigin.toVector()) - (sbox.value / 2.65f), tdistance);
                    tcollided++;
                } else tmisses++;

                if(checkFrom != null) {
                    fdistance = Math.min(new Vector(checkFrom.x, checkFrom.y, checkFrom.z)
                            .distance(fromOrigin.toVector()) - (sbox.value / 2.65f), fdistance);
                    fcollided++;
                } else fmisses++;
            }

            boolean usedFrom = false;
            if(fmisses <= tmisses) {
                misses = fmisses;
                collided = fcollided;
                distance = fdistance;
                usedFrom = true;
            } else {
                misses = tmisses;
                collided = tcollided;
                distance = tdistance;
            }

            if(distance == 69) {
                buffer-= buffer > 0 ? 0.01f : 0;
                debug("none collided: " + misses + ", " + entityLocs.size());
                return;
            }

            if(collided > 1 && data.lagInfo.lastPacketDrop.isPassed(2)) {
                if(distance > 3.1 &&
                        Kauri.INSTANCE.lastTickLag.isPassed(40)) {
                    if(++buffer > 4) {
                        vl++;
                        flag("distance=%.3f from=%s buffer=%.1f misses=%s",
                                distance, usedFrom, buffer, misses);
                        buffer = 4;
                    }
                } else buffer-= buffer > 0 ? .2f : 0;
            }

            debug("distance=%.3f from=%s buffer=%.2f ticklag=%s collided=%s",
                    distance, usedFrom, buffer, Kauri.INSTANCE.lastTickLag.getPassed(), collided);
        }
    }

    @Packet
    public void onArm9(WrappedInArmAnimationPacket packet) {
        buffer-= buffer > 0 ? 0.01 : 0;
    }

    private static CollisionBox getHitbox(Entity entity, KLocation loc) {
        return EntityData.getEntityBox(loc, entity);
    }
}