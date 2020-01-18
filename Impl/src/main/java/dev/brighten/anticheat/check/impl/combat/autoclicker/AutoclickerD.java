package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.math.cond.MaxInteger;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.EvictingList;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (D)", description = "Compares the current click cps to the average.",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 20)
public class AutoclickerD extends Check {

    private EvictingList<Long> cpsCounts = new EvictingList<>(50);
    private EvictingList<Double> errorList = new EvictingList<>(20);
    private long ltimestamp;
    private MaxInteger verbose = new MaxInteger(80);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - ltimestamp;

        if(delta > 300 || delta < 5) {
            ltimestamp = timeStamp;
            return;
        }

        if(cpsCounts.size() > 50) {
            try {
                double average = cpsCounts.stream().mapToLong(v -> v).average().orElseThrow(Exception::new);
                double std = Math.sqrt(cpsCounts.parallelStream().mapToDouble(v -> Math.pow(v - average, 2)).average()
                        .orElseThrow(Exception::new));

                double error = Math.abs((delta - average) / (std / Math.sqrt(cpsCounts.size())));

                errorList.add(error);

                double errorStd = MathUtils.stdev(errorList);

                if(errorStd < 4 && average > 4 && errorList.size() > 15) {
                    if(verbose.add(errorStd < 3 ? 4 : 2) > 22) {
                        vl++;
                        flag("errorstd=%1 error=%2 avg=%3 std=%4",
                                MathUtils.round(errorStd, 3),
                                MathUtils.round(error, 3), MathUtils.round(average, 3),
                                MathUtils.round(std, 4));
                    }
                } else verbose.subtract();

                debug("error=%3 avg=%1 std=%2 errorStd=%4 verbose=%5",
                        MathUtils.round(average, 3),
                        MathUtils.round(std, 3),
                        MathUtils.round(error, 4), MathUtils.round(errorStd, 4), verbose);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        cpsCounts.add(delta);
        ltimestamp = timeStamp;
    }
}
