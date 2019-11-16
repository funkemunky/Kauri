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

@CheckInfo(name = "Autoclicker (F)", description = "Looks for a flaw in autoclickers before they start clicking.",
        checkType = CheckType.AUTOCLICKER, punishVL = 5, developer = true, executable = false)
public class AutoclickerF extends Check {

    private long lastClick, lastMinimum, lastMaximum;
    private double lastAvg;
    private Interval<Long> clickValues = new Interval<>(0, 60);

    @Packet
    public void onAnimation(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastClick;

        if(delta > 400 || data.lagInfo.lastPacketDrop.hasNotPassed(1)) {
            lastClick = timeStamp;
            return;
        }

        if(clickValues.size() >= 50) {

            List<Long> distinct = new ArrayList<>();
            MiscUtils.listToStream(clickValues).forEach(distinct::add);

            double std = clickValues.std();
            double avg = clickValues.average();

            List<Long> withinDeviation = distinct
                    .stream()
                    .filter(l -> MathUtils.getDelta(l, avg) < std)
                    .collect(Collectors.toList());

            LongSummaryStatistics disStats = MiscUtils.listToStream(distinct).summaryStatistics();
            LongSummaryStatistics devStats = MiscUtils.listToStream(withinDeviation).summaryStatistics();

            debug("(longshit) avg=" + MathUtils.round(disStats.getAverage(), 4)
                    + " min=" + disStats.getMin() + " max=" + disStats.getMax() + " count=" + disStats.getCount());

            if(disStats.getMin() == lastMinimum && MathUtils.getDelta(avg, lastAvg) > 4) {
                if(disStats.getMax() == lastMaximum) vl++;
                if(vl++ > 4) {
                    flag("ur nigga ass using an autoclicker");
                }
                debug(Color.Green + "Flagged");
            } else vl-= vl > 0 ? 2 : 0;

            debug("avg=" + devStats.getAverage() + " max=" + devStats.getMax()
                    + " min=" + devStats.getMin() + " count=" + devStats.getCount());

            debug("vl=" + vl);

            lastMaximum = disStats.getMax();
            lastMinimum = disStats.getMin();
            lastAvg = disStats.getAverage();
            clickValues.clear();
            distinct.clear();
        } else clickValues.add(delta);

        lastClick = timeStamp;
    }
}