package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.handlers.PlayerSizeHandler;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Reach (A)", checkType = CheckType.HITBOX, punishVL = 5, description = "A simple distance check.")
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachA extends Check {

    private long lastUse;
    private LivingEntity target;
    private double buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastUse > 3 || data.playerInfo.creative || data.targetPastLocation.previousLocations.size() < 10) return;

        List<SimpleCollisionBox> origins = Stream.of(data.playerInfo.to.clone(), data.playerInfo.from.clone())
                .map(loc -> PlayerSizeHandler.getInstance().bounds(data.getPlayer(), loc.x, loc.y, loc.z))
                .collect(Collectors.toList());
        List<SimpleCollisionBox> targetBoxes = data.targetPastLocation
                .getEstimatedLocation(timeStamp,
                        data.lagInfo.transPing)
                .stream()
                .map(loc -> getHitbox(loc, target.getType()))
                .collect(Collectors.toList());

        double distance = 69;

        val bounds = getHitbox(new KLocation(0,0,0), target.getType());
        double width = bounds.max().setY(0).distance(bounds.min().setY(0));
        for (SimpleCollisionBox origin : origins) {
            //origin.draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));
            for (SimpleCollisionBox target : targetBoxes) {
                distance = Math.min(distance, origin.distance(target) - width);
                //target.draw(WrappedEnumParticle.FLAME, Collections.singleton(data.getPlayer()));
            }
        }

        if(data.lagInfo.lastPacketDrop.hasPassed(3)) {
            if (distance > 3.02 && distance != 69) {
                if (++buffer > 6) {
                    vl++;
                    flag("distance=%v.2 buffer=%v", distance, buffer);
                }
            } else buffer -= buffer > 0 ? 0.1 : 0;
        } else buffer-= buffer > 0 ? 0.02 : 0;

        debug("distance=%v.3 boxes=%v width=%v.2 buffer=%v", distance, targetBoxes.size(), width, buffer);
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
                && packet.getEntity() instanceof LivingEntity) {
            lastUse = timeStamp;
            target = (LivingEntity) packet.getEntity();
        }
    }

    private SimpleCollisionBox getHitbox(KLocation loc, EntityType type) {
        Vector vec = MiscUtils.entityDimensions.get(type).clone();

        if(vec == null) vec = new Vector(5, 0, 5);

        return new SimpleCollisionBox(loc.toVector(), loc.toVector()).expand(vec.getX(), 0, vec.getZ())
                .expandMax(0, vec.getY(), 0).expand(0.1, 0.1, 0.1);
    }
}
