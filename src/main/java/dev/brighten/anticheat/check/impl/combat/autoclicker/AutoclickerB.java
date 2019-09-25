package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Autoclicker (B)", description = "Checks for consistent patterns in CPS.")
public class AutoclickerB extends Check {

    private long lastClick;
    private Interval<Long> interval = new Interval<>(0, 50);
    private double lastStd, lastAvg;

    @Packet
    public void onPacket(WrappedInArmAnimationPacket packet, long timeStamp) {
        long range = timeStamp - lastClick;

        if(range < 1E6 && !data.playerInfo.breakingBlock) {
            if(interval.size() >= 25) {
                double avg = interval.average(), std = interval.std();
                double avgDelta = MathUtils.getDelta(lastAvg, avg), stdDelta = MathUtils.getDelta(std, lastStd);

                if(stdDelta < 2.5 && (avgDelta > 10 || (std > 25)) && avg < 118) {
                    debug(Color.Green + "Flag");
                    vl++;
                    if(vl > 6) {
                        punish();
                    } else if(vl > 3) flag("std=" + std + " avg=" + avg
                            + " stdDelta=" + stdDelta + " avgDelta=" + avgDelta + " ping=%p");
                } else vl-= vl > 0 ? 0.25 : 0;

                debug("avg=" + avg + " std=" + std + " size=" + interval.size());
                lastStd = std;
                lastAvg = avg;
                interval.clear();
            } else interval.add(range);
        }
        lastClick = timeStamp;
    }
}
