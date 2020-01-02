package dev.brighten.anticheat.check.impl.combat.hitbox;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.RayCollision;
import dev.brighten.anticheat.utils.RayTrace;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Hitboxes", description = "Ensures the player is looking at the target when attacking.",
        checkType = CheckType.HITBOX, punishVL = 15)
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
                    .getEstimatedLocation(0,Math.max(100, Math.min(150L, data.lagInfo.transPing)))
                    .stream()
                    .map(loc ->
                            loc.toLocation(data.getPlayer().getWorld()).clone()
                                    .add(0, data.playerInfo.sneaking ? 1.54f : 1.62f, 0))
                    .map(loc -> new RayCollision(loc.toVector(), loc.getDirection()))
                    .collect(Collectors.toList());

            List<BoundingBox> entityLocations = data.targetPastLocation
                    .getEstimatedLocation(data.lagInfo.transPing, 150L)
                    .stream()
                    .map(loc -> getHitbox(loc, data.target.getType()))
                    .collect(Collectors.toList());

            long collisions = 0;

            for (RayCollision ray : rayTrace) {
                collisions+= entityLocations.stream().filter(ray::isCollided).count();
            }

            if (collisions == 0) {
                if(vl++ > 10)  flag("collided=0 ping=%p tps=%t");
            } else vl -= vl > 0 ? 0.5 : 0;

            debug("collided=" + collisions);
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