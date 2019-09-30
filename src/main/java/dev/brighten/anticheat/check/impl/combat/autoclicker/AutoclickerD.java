package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.objects.Interval;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Autoclicker (D)", description = "It's an autoclicker check", checkType = CheckType.AUTOCLICKER)
public class AutoclickerD extends Check {

    private Interval<Long> interval = new Interval<>(0, 30);
    private long lastClick;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.lastBrokenBlock.hasPassed(5)) {
            long delta = timeStamp - lastClick;
            if(interval.size() > 25) {
                double disease = interval.std();
                double avg = interval.average();
                if(disease < 19) {
                    vl++;
                    if(vl > 13) {
                        punish();
                    } else if(vl > 5) {
                        flag("std=" + disease);
                    }

                } else vl-= vl > 0 ? 2 : 0;
                debug("disease=" + disease + " vl=" + vl + " avg=" + avg);
                interval.clear();
            } else interval.add(delta);
        }
        lastClick = timeStamp;
    }
}
