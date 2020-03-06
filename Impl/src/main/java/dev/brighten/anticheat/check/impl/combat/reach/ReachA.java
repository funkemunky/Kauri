package dev.brighten.anticheat.check.impl.combat.reach;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Reach (A)", checkType = CheckType.HITBOX, punishVL = 5, description = "A simple distance check.")
@Cancellable(cancelType = CancelType.ATTACK)
public class ReachA extends Check {

    private long lastUse;
    private LivingEntity target;
    private int buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(timeStamp - lastUse > 1 || data.playerInfo.creative || data.targetPastLocation.previousLocations.size() < 8) return;

        List<KLocation> origins = Arrays.asList(data.playerInfo.to.clone(), data.playerInfo.from.clone());
        List<Tuple<KLocation, Vector>> targetBoxes = data.targetPastLocation
                .getEstimatedLocation(data.lagInfo.transPing,
                        225L + (data.lagInfo.transPing - data.lagInfo.lastTransPing))
                .stream()
                .map(loc -> new Tuple<>(loc, getHitbox(target.getType())))
                .collect(Collectors.toList());

        double distance = 69;

        for (KLocation origin : origins) {
            for (Tuple<KLocation, Vector> tuple : targetBoxes) {
                distance = Math.min(distance, origin.toVector().setY(0).distance(tuple.one.toVector().setY(0))
                        - tuple.two.length());
            }
        }

        if(distance > 3.01 && distance != 69) {
            if(++buffer > 3) {
                vl++;
                flag("distance=%1 buffer=%2", MathUtils.round(distance, 3), buffer);
            }
        } else buffer = 0;

        debug("distance=%1 boxes=%2 buffer=%3", distance, targetBoxes.size(), buffer);
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        if(packet.getAction().equals(WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK)
                && packet.getEntity() instanceof LivingEntity) {
            lastUse = timeStamp;
            target = (LivingEntity) packet.getEntity();
        }
    }

    private Vector getHitbox(EntityType type) {
        Vector vec = MiscUtils.entityDimensions.get(type).clone();

        if(vec == null) vec = new Vector(5, 0, 5);

        return vec.setY(0);
    }
}
