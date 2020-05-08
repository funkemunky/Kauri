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
        if (data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(1))
            return;

        if (data.clickProcessor.getStd() < 15.d) {
            if(verbose.add(data.clickProcessor.getStd() < 9. ? 2 : 1) > 4) {
                vl++;
                this.flag("STD: " + data.clickProcessor.getStd());
            }
        } else verbose.subtract(1.5);
    }
}
