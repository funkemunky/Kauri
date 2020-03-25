package dev.brighten.anticheat.premium.impl.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, developer = true, maxVersion = ProtocolVersion.V1_8_9)
public class AutoclickerJ extends Check {

    private double buffer, lavg;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock || data.playerInfo.lastBlockPlace.hasNotPassed(1)) return;

        double threshold = data.clickProcessor.getStd() * 2;

        if(data.clickProcessor.getKurtosis() < 0 && data.clickProcessor.getSkew() > 0.02) {
            if(++buffer > 5) {
                vl++;
                flag("kurtosis=%v.4 std=%v.4 avg=%v.3",
                        data.clickProcessor.getKurtosis(), data.clickProcessor.getStd(),
                        data.clickProcessor.getAverage());
            }
        } else buffer-= buffer > 0 ? 0.5 : 0;
        debug("kurtosis=%v.4 std=%v.4 avg=%v.3",
                data.clickProcessor.getKurtosis(), data.clickProcessor.getStd(), data.clickProcessor.getSkew());
    }
}
