package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LongSummaryStatistics;

@RequiredArgsConstructor
public class ClickProcessor {
    public EvictingList<Long> cpsList = new EvictingList<>(20);
    @Getter
    private double std, nosqrtStd, average, nosqrtKurtosis, kurtosis;
    @Getter
    private long min, max, sum;
    private long lastTimestamp;

    private final ObjectData data;

    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        if(delta < 2000 && delta > 0) {
            cpsList.add(delta);
            LongSummaryStatistics summary = cpsList.parallelStream().mapToLong(v -> v).summaryStatistics();

            average = summary.getAverage();
            min = summary.getMin();
            max = summary.getMax();
            sum = summary.getSum();

            double max = 0;
            double totalStd = 0;
            for (Long val : cpsList) {
                totalStd+= Math.pow(val- average, 2);
                max = Math.max(max, Math.pow(val- average, 2));
            }

            kurtosis = Math.sqrt(max);
            std = Math.sqrt(totalStd / summary.getCount());
            nosqrtStd = totalStd / summary.getCount();
            nosqrtKurtosis = max;
        }
        lastTimestamp = timeStamp;
    }
}
