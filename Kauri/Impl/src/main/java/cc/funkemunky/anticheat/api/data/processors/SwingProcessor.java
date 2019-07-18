package cc.funkemunky.anticheat.api.data.processors;

import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Interval;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import lombok.Getter;

@Getter
public class SwingProcessor {
    private Interval<Long> interval = new Interval<>(0, 20);
    private PlayerData data;
    private long lastClick, ms, lastDequeueProcess, distinct, lastDistinct;
    private double std, max, min, average, lastAverage, averageDelta, lastStd, stdDelta, cps;

    public SwingProcessor(PlayerData data) {
        this.data = data;
    }

    public void onUpdate(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(!MiscUtils.shouldReturnArmAnimation(data)) {
            ms = timeStamp - lastClick;
            cps = 1000D / ms;
            if(interval.size() >= 20) {
                lastAverage = average;
                average = interval.average();
                averageDelta = MathUtils.getDelta(average, lastAverage);
                lastStd = std;
                std = interval.std();
                lastDistinct = distinct;
                distinct = interval.distinct();
                max = interval.max();
                min = interval.min();
                stdDelta = MathUtils.getDelta(std, lastStd);
                interval.clear();
                lastDequeueProcess = timeStamp;
            } else interval.add(ms);
        }
        lastClick = timeStamp;
    }
}
