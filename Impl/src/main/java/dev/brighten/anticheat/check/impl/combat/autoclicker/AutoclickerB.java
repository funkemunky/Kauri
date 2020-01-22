package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (B)", description = "A test check atm.", developer = true, executable = false,
        checkType = CheckType.AUTOCLICKER, punishVL = 40)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerB extends Check {

    private Interval interval = new Interval(100);
    private long lastTimestamp;
    private double lStd, lAvg, lRatio;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {

        long delta = timeStamp - lastTimestamp;

        if(delta > 2000 || delta < 3
                || data.playerInfo.breakingBlock
                || data.playerInfo.lastBrokenBlock.hasNotPassed(5)
                || data.playerInfo.lastBlockPlace.hasNotPassed(5)) {
            lastTimestamp = timeStamp;
            return;
        }

        if(interval.size() >= 20) {
            double avg = interval.average();
            double std = interval.std();
            double ratio = avg / std;
            double cps = 1000 / avg, lcps = 1000 / lAvg;

            boolean greater = (MathUtils.getDelta(cps, lcps) > 2 && MathUtils.getDelta(std, lStd) < 3);
            if((MathUtils.getDelta(ratio, lRatio) < 0.2 && MathUtils.getDelta(avg, lAvg) > 8)
                    || (MathUtils.getDelta(std, avg) < 7)
                    || greater) {
                vl++;
                if(vl > 5) {
                    flag("std=%1 avg=%2", std, avg);
                }
                debug(Color.Green + "Flagged");
            } else vl-= vl > 0 ? 0.5f : 0;

            debug("ratio=" + Color.Green + ratio + Color.Gray + " std=" + std + " avg=" + avg + " vl=" + vl);
            interval.clear();
            lStd = std;
            lAvg = avg;
            lRatio = ratio;
        } else interval.add(delta);

        lastTimestamp = timeStamp;
    }
}
