package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingList;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

@RequiredArgsConstructor
public class ClickProcessor {
    public EvictingList<Long> cpsList = new EvictingList<>(40);
    @Getter
    private double std, average, nosqrtKurtosis, kurtosis, skewness, median, variance;
    @Getter
    private long min, max, sum, zeros;
    private long lastTimestamp;
    @Getter
    private int outliers, lowOutliers, highOutliers;
    @Getter
    private List<Double> modes = new ArrayList<>();
    @Getter
    private Tuple<List<Double>, List<Double>> outliersTuple = new Tuple<>(new ArrayList<>(), new ArrayList<>());

    private TickTimer lastZeroCheck = new TickTimer(2);

    @Getter
    private boolean notReady;

    private final ObjectData data;

    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        if(delta < 600
                && !data.playerInfo.breakingBlock && data.playerInfo.lastBlockPlace.hasPassed(3)) {
            cpsList.add(delta);
            LongSummaryStatistics summary = cpsList.stream().mapToLong(v -> v).summaryStatistics();

            if(lastZeroCheck.hasPassed()) {
                zeros = cpsList.stream().filter(dt -> dt <= 2).count();
                if(cpsList.size() >= 20) {
                    outliersTuple = MiscUtils.getOutliers(cpsList);
                    outliers = (lowOutliers = outliersTuple.one.size()) + (highOutliers = outliersTuple.two.size());
                }
                lastZeroCheck.reset();
            }

            average = summary.getAverage();
            min = summary.getMin();
            max = summary.getMax();
            sum = summary.getSum();
            modes = MiscUtils.getModes(cpsList);
            median = MiscUtils.getMedian(cpsList);
            kurtosis = MiscUtils.getKurtosis(cpsList);
            //skew = MiscUtils.getSkewness(cpsList);
            variance = MiscUtils.varianceSquared(average, cpsList);
            skewness = (average - median) / std;
            std = Math.sqrt(variance);
            nosqrtKurtosis = max;
        }
        notReady = data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(3)
                || cpsList.size() < 22;
        lastTimestamp = timeStamp;
    }
}
