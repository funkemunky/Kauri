package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.AtomicDouble;
import dev.brighten.anticheat.utils.RayCollision;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Hitboxes", description = "Checks if the player attacks outside a player's hitbox.",
        checkType = CheckType.HITBOX, punishVL = 15)
@Cancellable(cancelType = CancelType.ATTACK)
public class Hitboxes extends Check {

    private static List<EntityType> allowedEntities = Arrays.asList(
            EntityType.ZOMBIE,
            EntityType.VILLAGER,
            EntityType.PLAYER,
            EntityType.SKELETON,
            EntityType.PIG_ZOMBIE,
            EntityType.WITCH,
            EntityType.CREEPER,
            EntityType.ENDERMAN);

    private long lastTimeStamp;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if (timeStamp - lastTimeStamp <= 4) {
            lastTimeStamp = timeStamp;
            return;
        }
        lastTimeStamp = timeStamp;

        if (checkParameters(data)) {

            List<RayCollision> rayTrace = data.pastLocation
                    .getPreviousRange(Math.max(110L, Math.round(data.lagInfo.transPing / 2D)))
                    .stream()
                    .peek(loc -> loc.y+=data.playerInfo.sneaking ? 1.54 : 1.62)
                    .map(loc -> new RayCollision(loc.toVector(),
                            MathUtils.getDirection(loc)))
                    .collect(Collectors.toList());

            List<BoundingBox> entityLocations = data.targetPastLocation
                    .getEstimatedLocation(data.lagInfo.transPing, 180L)
                    .stream()
                    .map(loc -> getHitbox(loc, data.target.getType()))
                    .collect(Collectors.toList());

            long collisions = 0;
            AtomicDouble distance = new AtomicDouble(10);

            for (RayCollision ray : rayTrace) {
                collisions+= entityLocations.stream().filter(bb -> {
                    Vector point;
                    if((point = ray.collisionPoint(bb)) != null) {
                        double dist = point.distance(ray.getOrigin().toVector());

                        distance.set(Math.min(dist, distance.get()));
                        return dist < 3.4f;
                    }
                    return false;
                }).count();
            }

            if (collisions == 0) {
                if(vl++ > 10)  flag("distance=%1 ping=%p tps=%t",
                        distance.get() != -1 ? distance.get() : "[none collided]");
            } else vl -= vl > 0 ? 0.5 : 0;

            debug("collided=" + collisions + " distance=" + distance.get());
        }
    }

    private static boolean checkParameters(ObjectData data) {
        return data.playerInfo.lastAttack.hasNotPassed(0)
                && data.target != null
                && Kauri.INSTANCE.lastTickLag.hasPassed(10)
                && allowedEntities.contains(data.target.getType())
                && !data.playerInfo.creative
                && data.playerInfo.lastTargetSwitch.hasPassed()
                && !data.getPlayer().getGameMode().equals(GameMode.CREATIVE);
    }

    private static BoundingBox getHitbox(KLocation loc, EntityType type) {
        Vector bounds = MiscUtils.entityDimensions.get(type);

        BoundingBox box = new BoundingBox(loc.toVector(), loc.toVector())
                .grow((float)bounds.getX(), 0, (float)bounds.getZ())
                .add(0,0,0,0,(float)bounds.getY(),0)
                .grow(0.02f,0.02f,0.02f);

        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
            return box.grow(0.1f,0.1f,0.1f);
        }

        return box;
    }
}