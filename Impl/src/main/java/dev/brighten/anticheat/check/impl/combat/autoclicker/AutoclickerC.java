package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (C)", description = "Checks if the autoclicker always stays within a specific range",
        checkType = CheckType.AUTOCLICKER, developer = true, punishVL = 20)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerC extends Check {

    private Interval interval = new Interval(10);

    private long lTimestamp;
    private double lavg, lstd;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lTimestamp;

        if(delta > 2000 || delta < 3 || data.playerInfo.lastBrokenBlock.hasNotPassed(5)) {
            lTimestamp = timeStamp;
            return;
        }

        if(interval.size() >= 10) {
            val stats = interval.getSummary();

            double avg = stats.getAverage();
            double std = interval.std();

            double deltaAvg = MathUtils.getDelta(avg, lavg), deltaStd = MathUtils.getDelta(std, lstd);
            if(deltaAvg < 8 && std > 60) {
                vl++;
                if(vl > 4) {
                    flag("avg=%1 std=%2", deltaAvg, deltaStd);
                }
            } else vl-= vl > 0 ? 0.2 : 0;
            debug("vl=" + vl + " avg=" + MathUtils.round(avg, 3)
                    + " std=" + MathUtils.round(std, 3));


            interval.clear();
            lstd = std;
            lavg = avg;
        } else interval.add(delta);
        lTimestamp = timeStamp;
    }
}
