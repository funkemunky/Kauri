package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, maxVersion = ProtocolVersion.V1_8_9, punishVL = 130, vlToFlag = 50)
public class AutoclickerJ extends Check {

    private double buffer;
    private long lastArm;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lastBlockPlace.hasNotPassed(1)) return;

        if(data.clickProcessor.getKurtosis() < 0
                && data.clickProcessor.getAverage() < 180
                && data.clickProcessor.getVariance() > 850
                && data.clickProcessor.getSkew() < 0.3
                && data.clickProcessor.cpsList.size() >= 30) {
            if(buffer++ > 40) {
                vl++;
                flag("k=%v.4 avg=%v.3 s=%v.3 v=%v.3 b=%v.1",
                        data.clickProcessor.getKurtosis(), data.clickProcessor.getAverage(),
                        data.clickProcessor.getSkew(), data.clickProcessor.getVariance(), buffer);
            }
        } else buffer-= buffer > 0 ? 4 : 0;
        long delta = timeStamp - lastArm;
        debug("kurtosis=%v.4 std=%v.4 avg=%v.3 skew=%v.3 variance=%v.3 buffer=%v.1 delta=%v",
                data.clickProcessor.getKurtosis(), data.clickProcessor.getStd(), data.clickProcessor.getAverage(),
                data.clickProcessor.getSkew(),
                data.clickProcessor.getVariance(),
                buffer, delta);
        lastArm = timeStamp;
    }
}
