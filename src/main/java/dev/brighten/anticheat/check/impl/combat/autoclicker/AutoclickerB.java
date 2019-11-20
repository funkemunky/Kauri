package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.TickTimer;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Autoclicker (B)", description = "A test check atm.", developer = true, executable = false,
        checkType = CheckType.AUTOCLICKER)
public class AutoclickerB extends Check {

    private Interval interval = new Interval(0, 100);
    private long lastTimestamp;
    private double lStd, lAvg;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {

        long delta = timeStamp - lastTimestamp;

        if(delta > 2000 || delta < 3) {
            lastTimestamp = timeStamp;
            return;
        }

        if(interval.size() >= 80) {
            double avg = interval.average();
            double std = interval.std();

            if(std < 40 && avg < 140) {
                if(vl++ > 3) {
                    flag("std=" + std + " avg=" + avg);
                }
                debug(Color.Green + "Flagged");
            } else vl-= vl > 0 ? 0.5f : 0;

            debug("std=" + std + " avg=" + avg + " vl=" + vl);
            interval.clear();
            lStd = std;
            lAvg = avg;
        } else interval.add(delta);

        lastTimestamp = timeStamp;
    }
}
