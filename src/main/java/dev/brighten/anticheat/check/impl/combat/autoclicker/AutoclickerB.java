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

    private Interval<Long> delta = new Interval<>(0, 25);

    private long lastTS;
    private double lAvg, lStd, lRange, lRatio;
    private TickTimer lastRatioChange = new TickTimer(20), lastStdChange = new TickTimer(20);

    @Packet
    public void onArmAnimation(WrappedInArmAnimationPacket packet, long timeStamp) {

        long deltaClick = timeStamp - lastTS;

        if(deltaClick > 2000L || deltaClick < 3) {
            lastTS = timeStamp;
            return;
        }

        if(delta.size() > 20) {
            delta.removeLast();
        }

        delta.addFirst(deltaClick);

        double std = delta.std();
        double avg = delta.average();
        double range = delta.max() - delta.min();

        double ratio = avg / std;

        if(std > 40
                && MathUtils.getDelta(ratio, lRatio) < 0.3
                && MathUtils.getDelta(std, lStd) > 2
                && MathUtils.getDelta(avg, lAvg) > 1) {
            vl++;
            debug(Color.Green + "kachow: " + vl);
        } else vl-= vl > 0 ? 0.25 : 0;

        debug("ratio=" + Color.Green + ratio + Color.Gray + "std=" + std + " avg=" + avg
                + " range=" + range + " passed=" + lastRatioChange.getPassed());
        lastTS = timeStamp;
        lStd = std;
        lAvg = avg;
        lRange = range;
        lRatio = ratio;
    }
}
