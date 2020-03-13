package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.*;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, developer = true)
public class AutoclickerJ extends Check {

    private EvictingList<Long> cpsList = new EvictingList<>(20);
    private long lastClick;
    private double buffer, lavg;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastClick;

        if(delta > 5 && delta < 1000)
        cpsList.add(delta);

        if(cpsList.size() >= 20) {
            LongSummaryStatistics summary = cpsList.parallelStream().mapToLong(l -> l).summaryStatistics();

            val average = summary.getAverage();

            double max = 0;
            double totalStd = 0;
            for (Long val : cpsList) {
                totalStd+= Math.pow(val- average, 2);
                max = Math.max(max, Math.pow(val- average, 2));
            }

            double std = totalStd / 40;

            if(max < 6000 && MathUtils.getDelta(average, lavg) > 0.4 && MathUtils.getDelta(std, max) > 1000) {
                if(buffer++ > 30) {
                    vl++;
                    flag("kurt=%v.3 avg=%v.3 std-%v.3 buffer=%v.1", max, average, std, buffer);
                }
            } else buffer-= buffer > 0 ? 0.5 : 0;

            debug("kurt=%v.3 avg=%v.3 std-%v.3 buffer=%v.1", max, average, std, buffer);
            lavg = average;
        }
        lastClick = timeStamp;
    }
}
