package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (E)", description = "Checks for impossible deviations (Elevated/funkemunky).",
        checkType = CheckType.AUTOCLICKER)
public class AutoclickerE extends Check {

    private Deque<Long> clickSamples = new LinkedList<>();

    private long lastSwing;
    private double stdDelta;
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
            if (std < 15.d) {
                if(std < 9.d) verbose.add(2.5);
                if(verbose.add() > 4) {
                    vl++;
                    this.flag("STD: " + std);
                }
            } else verbose.subtract(0.5);

            debug("std=%1 verbose=%2", std, verbose.value());

            this.clickSamples.clear();
        }

        this.lastSwing = timeStamp;
    }
}
