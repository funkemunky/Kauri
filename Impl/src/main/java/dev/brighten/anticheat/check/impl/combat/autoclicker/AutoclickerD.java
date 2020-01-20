package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.Tuple;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.EvictingList;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.stream.Collectors;

@CheckInfo(name = "Autoclicker (D)", description = "Compares the current click cps to the average.",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 20)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerD extends Check {

    private EvictingList<Long> cpsCounts = new EvictingList<>(40);
    private EvictingList<Tuple<Double, Double>> errorList = new EvictingList<>(25);
    private long ltimestamp;
    private double lerror;
    private MaxDouble verbose = new MaxDouble(80);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - ltimestamp;

        if(delta > 300 || delta < 5 || data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(3)
                || data.playerInfo.lastBrokenBlock.hasNotPassed(3)) {
            ltimestamp = timeStamp;
            return;
        }

        if(cpsCounts.size() > 20) {
            try {
                double average = cpsCounts.stream().mapToLong(v -> v).average().orElseThrow(Exception::new);
                double std = Math.sqrt(cpsCounts.parallelStream().mapToDouble(v -> Math.pow(v - average, 2)).average()
                        .orElseThrow(Exception::new));

                double error = Math.abs((delta - average) / (std / Math.sqrt(cpsCounts.size())));

                errorList.add(new Tuple<>(error, average));

                double errorStd = MathUtils.stdev(errorList.stream().map(v -> v.one).collect(Collectors.toList()));
                val summary = errorList.stream().mapToDouble(v -> v.two).summaryStatistics();

                double avgRange = summary.getMax() - summary.getMin();

                if(errorStd < 3.6 && average > 4 && error > 2 && MathUtils.getDelta(error, lerror) > 2
                        && average < 140 && avgRange < 14 && errorList.size() > 10) {
                    if(verbose.add(2) > 22) {
                        vl++;
                        flag("errorstd=%1 error=%2 avg=%3 std=%4",
                                MathUtils.round(errorStd, 3),
                                MathUtils.round(error, 3), MathUtils.round(average, 3),
                                MathUtils.round(std, 4));
                    }
                } else verbose.subtract(0.5);

                debug("error=%3 avg=%1 std=%2 errorStd=%4 range=%5 verbose=%6",
                        MathUtils.round(average, 2),
                        MathUtils.round(std, 3),
                        MathUtils.round(error, 2),
                        MathUtils.round(errorStd, 3),
                        MathUtils.round(avgRange, 2), verbose);

                lerror = error;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cpsCounts.add(delta);
        ltimestamp = timeStamp;
    }
}
