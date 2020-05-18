package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.utils.MiscUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
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

import java.util.DoubleSummaryStatistics;

@CheckInfo(name = "Aim (G)", description = "Checks the difference between angles.",
        checkType = CheckType.AIM, developer = true, enabled = false, punishVL = 30)
public class AimG extends Check {

    private EvictingList<Double> samples = new EvictingList<>(30);
    private int buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if ((!packet.isLook() && !packet.isPos())
                || data.target == null
                || data.playerInfo.lastAttack.hasPassed(1)) return;

        val origin = data.playerInfo.to.clone();

        origin.y+= data.playerInfo.sneaking ? 1.54 : 1.62;
        RayCollision ray = new RayCollision(origin.toVector(), MathUtils.getDirection(origin));

        SimpleCollisionBox box = getHitbox(data.target.getLocation(), data.target.getType());

        Vector collision = ray.collisionPoint(box);

        if(collision != null) {
            Vector centorOfBox = MathUtils.getCenterOfBox(box.toBoundingBox());

            samples.add(collision.distance(centorOfBox));

            if(samples.size() > 15) {
                val outliersTuple = MiscUtils.getOutliers(samples);

                int low = outliersTuple.one.size(), high = outliersTuple.two.size();
                DoubleSummaryStatistics summary = samples.stream().mapToDouble(v -> v).summaryStatistics();

                double mean = summary.getAverage(), std = MathUtils.stdev(samples),
                ble skewness = MiscUtils.getSkewnessApache(samples);
                median = MiscUtils.getMedian(samples);

                dou              double kurtosis = MiscUtils.getKurtosisApache(samples);

                if((Math.abs(kurtosis) < 0.1 && skewness > 0.8) || (std > 0.1 && (low + high) == 0)) {
                    if(++buffer > 5) {
                        vl++;
                        flag(20 * 20, "outliers=" + (low + high));
                    }
                    debug(Color.Green + "Flag: " + buffer);
                } else buffer = 0;

                debug("low=%v high=%v mean=%v.1 std=%v.2 kurt=%v.2 skewness=%v.2",
                        low, high, mean, std, kurtosis, skewness);
            }
        }
    }

    private static SimpleCollisionBox getHitbox(Location loc, EntityType type) {
        if(type.equals(EntityType.PLAYER)) {
            return new SimpleCollisionBox(loc.toVector(), 0.6, 1.8).expand(0.1, 0.1, 0.1);
        } else {
            Vector bounds = cc.funkemunky.api.utils.MiscUtils.entityDimensions.get(type);

            return new SimpleCollisionBox(loc.toVector(), 0, 0).expand(bounds.getX(), 0, bounds.getZ())
                    .expandMax(0, bounds.getY(), 0)
                    .expand(0.1, 0.1, 0.1);
        }
    }
}
