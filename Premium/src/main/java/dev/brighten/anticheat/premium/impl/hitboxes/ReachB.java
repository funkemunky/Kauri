package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (B)", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 8)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private long lastUse;
    private double buffer;
    private Entity entity;

    @Setting(name = "debug")
    private static boolean debug = false;

    @Packet
    public void onFly(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastUse < 1 && entity != null && entity instanceof Player) {
            ObjectData targetData = Kauri.INSTANCE.dataManager.getData((Player) entity);

            if(data.playerInfo.creative) return;

            KLocation originLoc = data.playerInfo.to.clone();

            originLoc.y+= data.playerInfo.sneaking ? 1.54 : 1.62;
            RayCollision ray = new RayCollision(originLoc.toVector(), MathUtils.getDirection(originLoc));

            List<SimpleCollisionBox> entityLocs = data.targetPastLocation.getEstimatedLocation(timeStamp,
                            data.lagInfo.transPing,
                            200L + Math.abs(data.lagInfo.transPing - data.lagInfo.lastTransPing))
                    .stream()
                    .map(ReachB::getHitbox).collect(Collectors.toList());

            double distance = 69;
            int misses = 0, collided = 0;
            if(debug) ray.draw(WrappedEnumParticle.CRIT, Bukkit.getOnlinePlayers());
            for (SimpleCollisionBox box : entityLocs) {
                if(debug) box.draw(WrappedEnumParticle.FLAME, Bukkit.getOnlinePlayers());
                val check = RayCollision.distance(ray, box);

                if(check == -1) {
                    misses++;
                    continue;
                } else collided++;
                distance = Math.min(distance, check);
            }

            if(distance == 69) {
                buffer-= buffer > 0 ? 0.01 : 0;
                return;
            }

            double subtraction = 0.0625;
            if(targetData != null)
                subtraction+= Math.min(0.2, (data.playerInfo.deltaXZ + targetData.playerInfo.deltaXZ) / 2.65);
            else subtraction+= 0.021;
            distance-= subtraction;

            if(collided > 1 && data.lagInfo.lastPacketDrop.hasPassed(2)) {
                if(distance > 3.02 &&
                        Kauri.INSTANCE.lastTickLag.hasPassed(40)) {
                    if(++buffer > 4) {
                        vl++;
                        flag("distance=%v.3 buffer=%v.1 misses=%v", distance, buffer, misses);
                    }
                } else buffer-= buffer > 0 ? 0.02 : 0;
            }

            debug("distance=%v.3 buffer=%v.1 ticklag=%v collided=%v subtraction=%v.4",
                    distance, buffer, Kauri.INSTANCE.lastTickLag.getPassed(), collided, subtraction);
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        lastUse = timeStamp;
        entity = packet.getEntity();
    }

    @Packet
    public void onArm9(WrappedInArmAnimationPacket packet) {
        buffer-= buffer > 0 ? 0.001 : 0;
    }

    private static SimpleCollisionBox getHitbox(KLocation loc) {
        return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(0.4f, 0.1f, 0.4f)
                .expandMax(0,1.8,0);
    }
}