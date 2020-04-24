package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@CheckInfo(name = "Autoclicker (I)", description = "Checks for impossible ratio consistency. Check by Elevated.",
        checkType = CheckType.AUTOCLICKER, enabled = false, developer = true)
public class AutoclickerI extends Check {

    private List<Long> clickSamples = new ArrayList<>();

    private long lastSwing;
    private double lstd;
    private MaxDouble verbose = new MaxDouble(10);

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delay = timeStamp - this.lastSwing;

        if (data.playerInfo.lookingAtBlock
                || data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(1))
            return;

        if (delay > 1L && delay < 300L && this.clickSamples.add(delay) && this.clickSamples.size() == 30) {
            double average = this.clickSamples.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);

            double stdDeviation = 0.0;

            for (Long click : this.clickSamples) {
                stdDeviation += Math.pow(click.doubleValue() - average, 2);
            }

            stdDeviation /= this.clickSamples.size();

            val std = Math.sqrt(stdDeviation);
            val deltaStd = Math.abs(std - lstd);
            if (deltaStd < 2.5) {
                if(verbose.add(std < 15 ? 2 : 1) > 4) {
                    vl++;
                    this.flag("std=%v.2 delta=%v.2 buffer=%v.1", std, deltaStd, verbose.value());
                }
            } else verbose.subtract(1);

            debug("std=%v.2 delta=%v.2 verbose=%v.1", std, deltaStd, verbose.value());

            lstd = std;
            this.clickSamples.clear();
        }

        this.lastSwing = timeStamp;
    }
}
