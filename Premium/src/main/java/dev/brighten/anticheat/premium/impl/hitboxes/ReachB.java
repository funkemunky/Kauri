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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@CheckInfo(name = "Reach (B)", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 8, planVersion = KauriVersion.ARA, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private float buffer;

    @Setting(name = "debug")
    private static boolean debug = false;

    @Packet
    public void onFly(WrappedInUseEntityPacket packet) {
        if(data.target != null) {
            if(data.playerInfo.creative) return;

            List<KLocation> entityLocs = data.entityLocPastLocation.getEstimatedLocation(
                    Kauri.INSTANCE.keepaliveProcessor.tick,
                    data.lagInfo.transPing + 1, 1, 3);

            double distance = 69;
            int misses = 0, collided = 0;
            Location toOrigin = data.playerInfo.to.toLocation(data.getPlayer().getWorld());

            toOrigin.setY(toOrigin.getY() + (data.playerInfo.sneaking ? 1.54 : 1.62));
            for (int i = 0; i < entityLocs.size(); i++) {
                KLocation loc = entityLocs.get(i);

                SimpleCollisionBox hitbox = (SimpleCollisionBox) getHitbox(data.target, loc);

                val copied = data.playerVersion.isBelow(ProtocolVersion.V1_9)
                        ? hitbox.copy().expand(0.1) : hitbox;
                AxisAlignedBB aabb = new AxisAlignedBB(copied);
                if(debug) copied.draw(WrappedEnumParticle.FLAME, Bukkit.getOnlinePlayers());
                Vec3D checkTo = aabb.rayTrace(toOrigin.toVector(), toOrigin.getDirection(), 10);

                if(checkTo != null) {
                    distance = Math.min(new Vector(checkTo.x, checkTo.y, checkTo.z)
                            .distanceSquared(toOrigin.toVector()), distance);
                    collided++;
                } else misses++;
            }

            if(distance != 69) distance = Math.sqrt(distance);

            boolean usedFrom = false;

            if(collided == 0) {
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

    private static CollisionBox getHitbox(Entity entity, KLocation loc) {
        return EntityData.getEntityBox(loc, entity);
    }
}