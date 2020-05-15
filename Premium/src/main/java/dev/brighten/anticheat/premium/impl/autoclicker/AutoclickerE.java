package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (E)", description = "Checks for hard to get deviations (Elevated/funkemunky).",
        checkType = CheckType.AUTOCLICKER, developer = true)
public class AutoclickerE extends Check {

    private MaxDouble verbose = new MaxDouble(10);

    @Packet
    public void onClick(WrappedInArmAnimationPacket packet) {
        if (data.clickProcessor.isNotReady())
            return;

        int modals = data.clickProcessor.getModes().size();
        double skewness = Math.abs(data.clickProcessor.getSkewness());

        if (modals > 1 && skewness > 0.5) {
            if(verbose.add(data.clickProcessor.getVariance() > 6000 ? 2 : 1) > 4) {
                vl++;
                this.flag("skewness=%v.2 modals=%v", skewness, modals);
            }
        } else verbose.subtract(.5);
    }
}
