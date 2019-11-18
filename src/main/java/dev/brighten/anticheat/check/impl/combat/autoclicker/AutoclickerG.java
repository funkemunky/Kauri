package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for a consistent range of CPS.",
        checkType = CheckType.AUTOCLICKER, developer = true, executable = false)
public class AutoclickerG extends Check {

    private Interval<Long> clickValues = new Interval<>(0, 60);
    private long lastClick, lastMinimum, lastMaximum;
    private double lastAvg;

    @Packet
    public void onAnimation(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastClick;

        if(delta > 400 || delta < 49) {
            lastClick = timeStamp;
            return;
        }

        if(clickValues.size() >= 50) {

            LongSummaryStatistics stats = MiscUtils.listToStream(clickValues).summaryStatistics();

            long range = stats.getMax() - stats.getMin(), lastRange = lastMaximum - lastMinimum;

            long deltaRange = MathUtils.getDelta(range, lastRange);

            if(stats.getAverage() < 125
                    && deltaRange < 18
                    && (MathUtils.getDelta(stats.getAverage(), lastAvg) > 5 || deltaRange < 6)) {
                if(vl++ > 5) {
                    flag("deltaRange=" + deltaRange + " range=" + range
                            + " avg=" + MathUtils.round(stats.getAverage(), 4));
                }
            } else vl-= vl > 0 ? 2 : 0;

            debug("avg=" + stats.getAverage() + " range=" + range + " lastRange=" + lastRange
                    + " vl=" + vl);

            lastMaximum = stats.getMax();
            lastMinimum = stats.getMin();
            lastAvg = stats.getAverage();
            clickValues.clear();
        } else clickValues.add(delta);

        lastClick = timeStamp;
    }
}
