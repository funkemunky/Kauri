package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (G)", description = "Checks for outliers in clicks.",
        checkType = CheckType.AUTOCLICKER, vlToFlag = 4, punishVL = 12)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerG extends Check {

    float buffer;
    @Packet
    public void check(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lookingAtBlock
                || data.clickProcessor.isNotReady()
                || data.playerInfo.lastBrokenBlock.isNotPassed(5)
                || data.playerInfo.lastBlockDigPacket.isNotPassed(1)
                || data.playerInfo.lastBlockPlacePacket.isNotPassed(1))
            return;

        int low = data.clickProcessor.getLowOutliers(), high = data.clickProcessor.getHighOutliers();
        double cpsAvg = 20L / Math.max(0.0001, data.clickProcessor.getMean()); //I used long for 1000 since long is 64 bit with double.
        if((low + high) == 0 && data.clickProcessor.getMean() < 2.52) {
            if((buffer < 20 ? ++buffer : buffer) > 6) {
                vl++;
                flag(20 * 30,
                        "buffer=%.1f avgCps=%.1f std=%.2f low=%s high=%s", buffer, cpsAvg,
                        data.clickProcessor.getStd(), low, high);
            }
        } else buffer-= buffer > 0 ? 0.75f : 0;
        debug("buffer=%s low=%s high=%s avg=%.2f cpsAvg=%.1f",
                buffer, low, high, data.clickProcessor.getMean(), cpsAvg);
    }
}
