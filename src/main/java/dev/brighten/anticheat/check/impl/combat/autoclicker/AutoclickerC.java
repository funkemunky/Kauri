package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Autoclicker (C)", description = "Checks for abnormally consistent CPS.")
public class AutoclickerC extends Check {

    private long lastClick;
    private double lastAvg;
    private Interval<Long> interval = new Interval<>(0, 12);
    @Packet
    public void onPacket(WrappedInArmAnimationPacket packet) {

        long delta = System.currentTimeMillis() - lastClick;

        if(interval.size() >= 10 && !data.playerInfo.breakingBlock) {
            double avg = interval.average();

            if(MathUtils.getDelta(avg, lastAvg) <= 0.4) {
                vl++;

                if(vl > 20) {
                    punish();
                } else if(vl > 9) {
                    flag("avg=" + avg + " ping=%p tps=%t");
                }
            } else vl = 0;

            debug("vl=" + vl + " avg=" + avg + " lAvg=" + lastAvg);
            interval.clear();
            lastAvg = avg;
        } else interval.add(delta);

        lastClick = System.currentTimeMillis();
    }
}
