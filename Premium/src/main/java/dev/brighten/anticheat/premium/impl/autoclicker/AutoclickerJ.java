package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Autoclicker (J)", description = "Checks for impossible deviations. By Elevated.",
        checkType = CheckType.AUTOCLICKER, developer = true, vlToFlag = 1, punishVL = 10)
public class AutoclickerJ extends Check {

    private long lastSwing;
    private double lastAverage;

    private final Deque<Long> swingSamples = new LinkedList<>();

    @Packet
    public void onPacket(WrappedInArmAnimationPacket packet, long timeStamp) {
        final long now = System.currentTimeMillis();
        final long delay = now - this.lastSwing;

        // Make sure the player isn't digging, make sure the delay got added to the sample and the sample size is 20
        if (!data.playerInfo.lookingAtBlock && !data.playerInfo.breakingBlock
                && data.playerInfo.lastBlockPlace.hasPassed(4)
                && this.swingSamples.add(delay) && this.swingSamples.size() == 20) {
            // Get the delay average
            final double average = this.swingSamples.stream().mapToLong(l -> l).average().orElse(0.0);

            // Get the swing deviation
            final double totalSwings = this.swingSamples.stream().mapToLong(change -> change).asDoubleStream().sum();
            final double mean = totalSwings / this.swingSamples.size();
            final double deviation = this.swingSamples.stream().mapToLong(change -> change).mapToDouble(change -> Math.pow(change - mean, 2)).sum();

            // Impossible (technically)
            double sdeviation = Math.sqrt(deviation);
            if (sdeviation < 150.0 && average > 100.0 && Math.abs(average - this.lastAverage) <= 5.0) {
                vl++;
                flag("dev=%1 avg=%2", sdeviation, average);
            }

            debug("sdev=%1 avg=%2 mean=%3", sdeviation, average, mean);
            // Pass average and clear list
            this.lastAverage = average;
            this.swingSamples.clear();
        }

        // Set last swing-timestamp
        this.lastSwing = now;
    }
}
