package dev.brighten.anticheat.premium.impl.hitboxes;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Reach (B)", description = "Ensures the reach of a player is legitimate.",
        checkType = CheckType.HITBOX, punishVL = 10, developer = true)
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachB extends Check {

    private long lastUse;
    private double buffer;
    private Entity entity;

    @Packet
    public void onFly(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastUse < 1 && entity != null && entity instanceof Player) {
            ObjectData targetData = Kauri.INSTANCE.dataManager.getData((Player) entity);

            if(targetData == null || data.playerInfo.creative) return;

            List<RayCollision> origin = Stream.of(data.playerInfo.from.clone(), data.playerInfo.to.clone())
                    .peek(loc -> loc.y+= data.playerInfo.sneaking ? 1.54 : 1.62)
                    .map(loc -> new RayCollision(loc.toVector(), MathUtils.getDirection(loc)))
                    .collect(Collectors.toList());

            List<SimpleCollisionBox> entityLocs = targetData.pastLocation
                    .getEstimatedLocation(data.lagInfo.ping, 200L).stream()
                    .map(ReachB::getHitbox).collect(Collectors.toList());

            double distance = 69;
            for (RayCollision ray : origin) {
                for (SimpleCollisionBox box : entityLocs) {
                    distance = Math.min(distance, RayCollision.distance(ray, box));
                }
            }

            if(distance == -1 || distance == 69) {
                buffer-= buffer > 0 ? 0.01 : 0;
                return;
            }

            if(distance > 3.001) {
                if(++buffer > 3) {
                    vl++;
                    flag("distance=%1 buffer=%2", MathUtils.round(distance, 3),
                            MathUtils.round(buffer, 1));
                }
            } else buffer-= buffer > 0 ? 0.05 : 0;

            debug("distance=%1 buffer=%2", distance, buffer);
        }
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        lastUse = timeStamp;
        entity = packet.getEntity();
    }

    private static SimpleCollisionBox getHitbox(KLocation loc) {
        return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(0.4f, 0.1f, 0.4f)
                .expandMax(0,1.8,0);
    }
}