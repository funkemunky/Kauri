package dev.brighten.anticheat.processing;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import cc.funkemunky.api.utils.TickTimer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ClickProcessor {
    public EvictingList<Long> cpsList = new EvictingList<>(40);
    @Getter
    private double std, mean, kurtosis, skewness, median, variance;
    @Getter
    private long min, max, sum, zeros;
    @Getter
    private int outliers, lowOutliers, highOutliers, distinct;
    @Getter
    private List<Double> modes = new ArrayList<>();
    @Getter
    private Tuple<List<Double>, List<Double>> outliersTuple = new Tuple<>(new ArrayList<>(), new ArrayList<>());

    private TickTimer lastZeroCheck = new TickTimer(1);

    @Getter
    private boolean notReady;

    private final ObjectData data;
    private int flyingTicks;

    public void onFlying(WrappedInTransactionPacket packet) {
        flyingTicks++;
    }

    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = flyingTicks;

        if(data.playerInfo.breakingBlock) cpsList.clear();

        if(delta < 15
                && !data.playerInfo.breakingBlock
                && !data.playerInfo.lookingAtBlock
                && data.playerInfo.lastBlockPlacePacket.isPassed(1)
                && data.playerInfo.lastBlockDigPacket.isPassed(1)
                && data.playerInfo.lastBlockPlace.isPassed(3)) {
            cpsList.add(delta);

            zeros = cpsList.stream().filter(dt -> dt <= 2).count();
            if(cpsList.size() >= 20) {
                outliersTuple = MiscUtils.getOutliers(cpsList);
                outliers = (lowOutliers = outliersTuple.one.size()) + (highOutliers = outliersTuple.two.size());
            }

            min = Long.MAX_VALUE;
            max = Long.MIN_VALUE;
            sum = 0;
            for (Long v : cpsList) {
                sum+= v;
                if(v > 20) {
                    min = Math.min(v, min);
                    max = Math.max(v, max);
                }
            }

            mean = sum / (double)Math.max(1, cpsList.size());
            modes = MiscUtils.getModes(cpsList);
            median = MiscUtils.getMedian(cpsList);

            distinct = (int)cpsList.stream().mapToLong(vlv -> vlv).distinct().count();

            std = 0;
            for (Long v : cpsList) std+= Math.pow(v - mean, 2);

            variance = std / (long)cpsList.size();
            std = Math.sqrt(std / (long)cpsList.size());
            kurtosis = MiscUtils.getKurtosis(cpsList);
            skewness = MiscUtils.getSkewnessApache(cpsList);
        }
        notReady = data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.isNotPassed(3)
                || cpsList.size() < 22;

        flyingTicks = 0;
    }
}
