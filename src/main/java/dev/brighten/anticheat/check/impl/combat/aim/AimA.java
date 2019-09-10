package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.Comparator;

@CheckInfo(name = "Aim (Type A)", description = "A check that detects aim.")
public class AimA extends Check {
    private Interval<Double> offsetInterval = new Interval<>(0, 30);

    private double lastStd;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.entitiesNearPlayer.size() > 0 && packet.isLook()) {
            LivingEntity closest = data.entitiesNearPlayer.stream()
                    .min(Comparator.comparing(entity -> entity
                            .getLocation()
                            .toVector()
                            .distance(data.playerInfo.to.toVector())))
                    .get();

            World world = data.getPlayer().getWorld();
            double[] offset = MathUtils.getOffsetFromLocation(data.playerInfo.to
                    .toLocation(world)
                    .add(0, data.getPlayer().getEyeHeight(),0), closest.getEyeLocation());

            if(offsetInterval.size() > 25) {
                double avg = offsetInterval.average();
                double std = offsetInterval.std();
                double range = offsetInterval.max() - offsetInterval.min();
                double stdDelta = MathUtils.getDelta(std, lastStd);

                if(stdDelta < 5 || std < 10) {
                    debug(Color.Green + "Flag");
                }

                debug("avg=" + avg + " std=" + std + " range=" + range);

                lastStd = std;
                offsetInterval.clear();
            } else offsetInterval.add(offset[0] + offset[1]);
        }
    }
}
