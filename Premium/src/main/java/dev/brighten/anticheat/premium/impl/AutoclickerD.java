package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Autoclicker (D)", description = "Oscillation check by Abigail.",
        checkType = CheckType.AUTOCLICKER, punishVL = 15, vlToFlag = 4)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerD extends Check {

    private long ltimeStamp;
    private List<Long> delays = new ArrayList<>();
    private List<Long> samples = new ArrayList<>();
    private int verbose;
    private double lavg, lstd, lrange;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.lastBrokenBlock.hasNotPassed(3)
                || data.playerInfo.lastBlockPlace.hasNotPassed(1)) {
            ltimeStamp = timeStamp;
            return;
        }
        long delta = timeStamp - ltimeStamp;

        if (delta < 400) samples.add(delta);

        if (samples.size() >= 10) {
            val sampleSummary = samples.stream().mapToLong(v -> v).summaryStatistics();

            long osc = (sampleSummary.getMax() + sampleSummary.getMin()) / 2;

            delays.add(osc);
            if (delays.size() >= 5) {
                List<Double> list = new ArrayList<>();

                delays.stream()
                        .mapToDouble(v -> v)
                        .forEach(list::add);
                double std = MathUtils.stdev(list);
                val summary = delays.stream().mapToDouble(v -> v).summaryStatistics();
                double avg = summary.getAverage();
                double range = summary.getMax() - summary.getMin();

                if ((std < 20 || MathUtils.getDelta(std, lstd) < 0.3 || range <= 3) && Math.abs(range - lrange) > 1
                        && (MathUtils.getDelta(avg, lavg) > 5 || range > 31)) {
                    verbose++;
                    if (verbose > 3) {
                        vl++;
                        flag("std=%1 avg=%2 range=%3 ping=%p tps=%t", std, avg, range);
                    }
                } else verbose = 0;

                debug("std=" + std + " avg=" + avg + " range= " + range + " verbose=" + verbose);
                delays.clear();
                lavg = avg;
                lrange = range;
                lstd = std;
            }
            samples.clear();
        }
        ltimeStamp = timeStamp;
    }
}
