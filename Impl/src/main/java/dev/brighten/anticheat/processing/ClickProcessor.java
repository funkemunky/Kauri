package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
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
    public ConcurrentEvictingList<Long> cpsList = new ConcurrentEvictingList<>(40);
    @Getter
    private double std, average, nosqrtKurtosis, kurtosis, skew, variance;
    @Getter
    private long min, max, sum, zeros;
    private long lastTimestamp;

    private final ObjectData data;

    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        if(delta < 600
                && !data.playerInfo.breakingBlock && data.playerInfo.lastBlockPlace.hasPassed(3)) {
            cpsList.add(delta);
            LongSummaryStatistics summary = cpsList.parallelStream().mapToLong(v -> v).summaryStatistics();

            zeros = cpsList.parallelStream().filter(dt -> dt <= 2).count();

            average = summary.getAverage();
            min = summary.getMin();
            max = summary.getMax();
            sum = summary.getSum();

            val array = cpsList.stream().mapToDouble(l -> l).toArray();
            kurtosis = new Kurtosis().evaluate(array);
            skew = new Skewness().evaluate(array);
            variance = new Variance().evaluate(array);
            std = Math.sqrt(variance);
            nosqrtKurtosis = max;
        }
        lastTimestamp = timeStamp;
    }
}
