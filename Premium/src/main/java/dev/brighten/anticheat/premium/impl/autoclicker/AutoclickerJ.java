package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, developer = true, maxVersion = ProtocolVersion.V1_8_9)
public class AutoclickerJ extends Check {

    private double buffer;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock || data.playerInfo.lastBlockPlace.hasNotPassed(1)) return;

        if(data.clickProcessor.getKurtosis() < 0
                && data.clickProcessor.getVariance() < 2400
                && data.clickProcessor.getVariance() > 900L
                && Math.abs(data.clickProcessor.getSkew()) > 0.2) {
            if((buffer+= data.clickProcessor.getSkew() > 0.3
                    ? 1 : 0.5) > 15) {
                vl++;
                flag("k=%v.4 avg=%v.3 s=%v.3 v=%v.3 b=%v.1",
                        data.clickProcessor.getKurtosis(), data.clickProcessor.getAverage(),
                        data.clickProcessor.getSkew(), data.clickProcessor.getVariance(), buffer);
            }
        } else buffer-= buffer > 0 ? 2 : 0;
        debug("kurtosis=%v.4 std=%v.4 avg=%v.3 skew=%v.3 variance=%v.3 buffer=%v.1",
                data.clickProcessor.getKurtosis(), data.clickProcessor.getStd(), data.clickProcessor.getAverage(),
                data.clickProcessor.getSkew(),
                data.clickProcessor.getVariance(),
                buffer);
    }
}
