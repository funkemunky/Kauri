package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.DoubleSummaryStatistics;

@CheckInfo(name = "Aim (I)", description = "Checks for weird deviations in rotation.", checkType = CheckType.AIM,
        developer = true)
public class AimI extends Check {

    private Interval interval = new Interval(50);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.moveProcessor.deltaX > 0) {
            if(interval.size() > 40) {
                DoubleSummaryStatistics summary = interval.getSummary();

                double range = summary.getMax() - summary.getMin();
                float std = (float) interval.std(), avg = (float) summary.getAverage();

                debug("range=" + range + " std=" + std + " avg=" + avg);
                interval.clear();
            } else interval.add(data.moveProcessor.deltaX);
        }
    }
}
