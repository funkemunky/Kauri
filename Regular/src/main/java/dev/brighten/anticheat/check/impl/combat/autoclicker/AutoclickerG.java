package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for outliers in clicks.",
        checkType = CheckType.AUTOCLICKER, vlToFlag = 4, punishVL = 18)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerG extends Check {

    float buffer;
    @Packet
    public void check(WrappedInArmAnimationPacket packet) {
        if (data.clickProcessor.isNotReady()) return;

        int low = data.clickProcessor.getLowOutliers(), high = data.clickProcessor.getHighOutliers();
        double cpsAvg = 20L / Math.max(0.0001, data.clickProcessor.getMean()); //I used long for 1000 since long is 64 bit with double.
        if((low + high) == 0 && data.clickProcessor.getMean() < 2.52) {
            if((buffer < 20 ? ++buffer : buffer) > 6) {
                vl++;
                flag(20 * 30,
                        "buffer=%v avgCps=%v.1 std=%v.2 low=%v high=%v.", buffer, cpsAvg,
                        data.clickProcessor.getStd(), low, high);
            }
        } else buffer-= buffer > 0 ? 0.75f : 0;
        debug("buffer=%v low=%v high=%v avg=%v.2 cpsAvg=%v.1",
                buffer, low, high, data.clickProcessor.getMean(), cpsAvg);
    }
}
