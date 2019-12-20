package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Autoclicker (E)", description = "Oscillation check. Credits to Abigail.",
        checkType = CheckType.AUTOCLICKER, punishVL = 10)
public class AutoclickerE extends Check {

    private long ltimeStamp;
    private List<Long> delays = new ArrayList<>();
    private List<Long> samples = new ArrayList<>();
    private int verbose;
    private double lavg;

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
                double avg = delays.stream().mapToDouble(v -> v).summaryStatistics().getAverage();

                if (std < 20 && MathUtils.getDelta(avg, lavg) > 1) {
                    verbose++;
                    if (verbose > 2) {
                        vl++;
                        flag("std=" + std);
                    }
                } else verbose = 0;

                debug("std=" + std + " verbose=" + verbose);
                delays.clear();
                lavg = avg;
            }
            samples.clear();
        }
        ltimeStamp = timeStamp;
    }
}
