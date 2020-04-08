package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingList;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.LongSummaryStatistics;

@RequiredArgsConstructor
public class ClickProcessor {
    public EvictingList<Long> cpsList = new EvictingList<>(40);
    @Getter
    private double std, nosqrtStd, average, nosqrtKurtosis, kurtosis, skew, variance;
    @Getter
    private long min, max, sum;
    private long lastTimestamp;

    private final ObjectData data;

    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        if(delta < 2000 && delta > 10
                && !data.playerInfo.breakingBlock && data.playerInfo.lastBlockPlace.hasPassed(3)) {
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

            val array = cpsList.stream().mapToDouble(l -> l).toArray();
            kurtosis = new Kurtosis().evaluate(array);
            skew = new Skewness().evaluate(array);
            variance = new Variance().evaluate(array);
            std = Math.sqrt(totalStd / summary.getCount());
            nosqrtStd = totalStd / summary.getCount();
            nosqrtKurtosis = max;
        }
        lastTimestamp = timeStamp;
    }
}
