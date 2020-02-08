package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumParticle;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.math.RayTrace;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CheckInfo(name = "Killaura (D)", description = "Checks the angle of some mega doo doo.",
        checkType = CheckType.KILLAURA, developer = true)
public class KillauraD extends Check {

    private List<Vector> collisions = new ArrayList<>();
    private List<Float> differences = new ArrayList<>();
    private double verbose;

    private static List<EntityType> allowedEntities = Arrays.asList(
            EntityType.ZOMBIE,
            EntityType.VILLAGER,
            EntityType.PLAYER,
            EntityType.SKELETON,
            EntityType.PIG_ZOMBIE,
            EntityType.WITCH,
            EntityType.CREEPER,
            EntityType.ENDERMAN);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.target == null
                || data.targetPastLocation.previousLocations.size() <= 5
                || data.playerInfo.lastAttack.hasPassed(0)) return;

        KLocation entityLoc = data.targetPastLocation.getPreviousLocation(data.lagInfo.transPing).clone();

        SimpleCollisionBox entityBox = getHitbox(entityLoc, data.target.getType());

        KLocation origin = data.playerInfo.to.clone();

        origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;

        RayCollision collision = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

        Vector point;

        if((point = collision.collisionPoint(entityBox)) != null) {
            collisions.add(point);
            differences.add(data.playerInfo.deltaYaw + Math.abs(data.playerInfo.deltaPitch));

            if(collisions.size() >= 40) {
                Vector mid = entityLoc.toVector();
                mid.add(new Vector(0, (entityBox.yMax - entityBox.yMin) / 2., 0));

                val distances = collisions.stream().map(vec -> vec.distance(mid))
                        .collect(Collectors.toList());

                val yawSummary = differences.stream().mapToDouble(v -> v).summaryStatistics();

                val summary = distances.stream().mapToDouble(v -> v).summaryStatistics();

                val range = summary.getMax() - summary.getMin();

                val std = MathUtils.stdev(distances);

                val yawRange = yawSummary.getMax() - yawSummary.getMin();

                val yawAvg = yawSummary.getAverage();

                if(std < (yawAvg > 9 ? 2.7 : 1.8) && range > 1 && yawRange > 11) {
                    if(verbose++ > 2) {
                        vl++;
                        flag("avg=%1 std=%2 range=%3", summary.getAverage(), std, range);
                    }
                } else verbose-= verbose > 0 ? 0.1 : 0;
                debug("avg=%1 std=%2 range=%3 yawRange=%4 yawAvg=%5",
                        summary.getAverage(), std, range, yawRange, yawAvg);
                collisions.clear();
                differences.clear();
            }
        }


    }

    private static SimpleCollisionBox getHitbox(KLocation loc, EntityType type) {
        org.bukkit.util.Vector bounds = MiscUtils.entityDimensions.get(type);

        return new SimpleCollisionBox(loc.toVector(), loc.toVector())
                .expand(bounds.getX(), 0, bounds.getZ())
                .expand(0, bounds.getY(), 0).expand(0.1f,0.1f,0.1f);
    }
}
