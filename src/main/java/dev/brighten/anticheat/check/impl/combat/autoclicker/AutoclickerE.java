package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.processing.MovementProcessor;
import dev.brighten.anticheat.utils.MiscUtils;

@CheckInfo(name = "Autoclicker (E)", description = "Checks for the range of autoclicker.", punishVL = 15, executable = false)
public class AutoclickerE extends Check {

    private long lastClick, lastDelta;
    private double lastStd;
    private Interval<Long> interval = new Interval<>(0, 45);

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        long delta = timeStamp - lastClick;

        if(delta > 140) {
            lastClick = timeStamp;
            vl-= vl > 0 ? 0.02 : 0;
            return;
        }

        float gcd =
                MiscUtils.gcd(delta * MovementProcessor.offset, lastDelta * MovementProcessor.offset);
        long shit = (long) (gcd / MovementProcessor.offset);

        if(interval.size() >= 25) {
            double std = interval.std();
            double avg = interval.average();

            if((std < avg || MathUtils.getDelta(std, lastStd) < 3) || (MathUtils.getDelta(std, avg) < 3 && std > 35)) {
                if(vl++ > 8) {
                    flag("std=" + std + " avg=" + avg);
                }
            } else vl-= vl > 0 ? 0.1 : 0;

            debug("std=" + std + " avg=" + avg + " vl=" + vl);
            interval.clear();
            lastStd = std;
        } else interval.add(shit);

        lastDelta = delta;
        lastClick = timeStamp;
    }
}
