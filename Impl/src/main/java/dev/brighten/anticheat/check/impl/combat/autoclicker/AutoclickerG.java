package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.api.check.CheckType;
import lombok.val;

import java.util.concurrent.TimeUnit;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for outliers in clicks.",
        checkType = CheckType.AUTOCLICKER, vlToFlag = 10, developer = true, punishVL = 80)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerG extends Check {

    float buffer;
    @Packet
    public void check(WrappedInArmAnimationPacket packet) {
        if (data.clickProcessor.isNotReady()) return;

        int low = data.clickProcessor.getLowOutliers(), high = data.clickProcessor.getHighOutliers();
        double cpsAvg = 1000L / data.clickProcessor.getAverage(); //I used long for 1000 since long is 64 bit with double.
        if((low + high) == 0) {
            if((buffer < 20 ? ++buffer : buffer) > 6) {
                vl++;
                flag(20 * 30,
                        "buffer=%v low=%v high=%v.", buffer, low, high);
            }
        } else buffer-= buffer > 0 ? 0.75f : 0;
        debug("buffer=%v low=%v high=%v avg=%v.2 cpsAvg=%v.1",
                buffer, low, high, data.clickProcessor.getAverage(), cpsAvg);
    }
}
