package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Autoclicker (D)", description = "Oscillation check by Abigail.",
        checkType = CheckType.AUTOCLICKER, punishVL = 15, executable = false, vlToFlag = 4)
@Cancellable(cancelType = CancelType.INTERACT)
//TODO Redo with a proper oscillation alogorithim (actually was done in exile before).
public class AutoclickerD extends Check {

    private long ltimeStamp;
    private final List<Long> delays = new ArrayList<>(),
            samples = new ArrayList<>();
    private double lavg, lstd, verbose;

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.clickProcessor.isNotReady()) {
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

                if (std < 18 && (MathUtils.getDelta(avg, lavg) > 5 || MathUtils.getDelta(std, lstd) < 0.4 || std < 3)) {
                    if ((verbose+= data.clickProcessor.getStd() < 30 ? 0.5 : 1) > 4) {
                        vl++;
                        flag("std=%v.2 avg=%v.2 ping=%p tps=%t", std, avg);
                    }
                } else verbose = 0;

                debug("std=%v.2 avg=%v.2 verbose=%v", std, avg, verbose);
                delays.clear();
                lavg = avg;
                lstd = std;
            }
            samples.clear();
        }
        ltimeStamp = timeStamp;
    }
}
