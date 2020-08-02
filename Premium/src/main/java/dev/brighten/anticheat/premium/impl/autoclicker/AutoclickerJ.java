package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, maxVersion = ProtocolVersion.V1_8_9, punishVL = 130, vlToFlag = 50,
        developer = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerJ extends Check {

    private double buffer;
    private long lastArm;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.clickProcessor.isNotReady()) return;

        double skew = Math.abs(data.clickProcessor.getSkewness());
        if(data.clickProcessor.getKurtosis() < 0
                && data.clickProcessor.getMean() < 150L
                && skew < 0.1
                && (data.clickProcessor.getZeros() <= 1 ||  data.clickProcessor.getMean() <= 50.4)) {
            if(buffer++ > 40) {
                vl++;
                flag(20 * 40, "k=%v.4 avg=%v.3 s=%v.3 v=%v.3 b=%v.1 zeros=%v",
                        data.clickProcessor.getKurtosis(), data.clickProcessor.getMean(),
                        data.clickProcessor.getSkewness(), data.clickProcessor.getVariance(), buffer,
                        data.clickProcessor.getZeros());
            }
        } else buffer-= buffer > 0 ? 2 : 0;
        long delta = timeStamp - lastArm;
        debug("kurtosis=%v.4 std=%v.4 avg=%v.3 skew=%v.3 variance=%v.3 buffer=%v.1 delta=%v zeros=%v",
                data.clickProcessor.getKurtosis(), data.clickProcessor.getStd(), data.clickProcessor.getMean(),
                data.clickProcessor.getSkewness(),
                data.clickProcessor.getVariance(),
                buffer, delta, data.clickProcessor.getZeros());
        lastArm = timeStamp;
    }
}
