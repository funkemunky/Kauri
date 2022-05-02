package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (J)", description = "Checks the kurtosis of a player's click pattern.",
        checkType = CheckType.AUTOCLICKER, maxVersion = ProtocolVersion.V1_8_9, punishVL = 80, vlToFlag = 50)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerJ extends Check {

    private double buffer;
    private long lastArm;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet, long timeStamp) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lookingAtBlock
                || data.clickProcessor.isNotReady()
                || data.playerInfo.lastBrokenBlock.isNotPassed(5)
                || data.playerInfo.lastBlockDigPacket.isNotPassed(1)
                || data.playerInfo.lastBlockPlacePacket.isNotPassed(1))
            return;

        double skew = Math.abs(data.clickProcessor.getSkewness());
        if(data.clickProcessor.getKurtosis() < 0
                && data.clickProcessor.getMean() < 3
                && skew < 0.15
                && (data.clickProcessor.getZeros() <= 1 ||  data.clickProcessor.getMean() <= 1)) {
            if(buffer++ > 40) {
                vl++;
                flag(20 * 40, "k=%.4f avg=%.3f s=%.3f v=%.3f b=%.1f zeros=%s",
                        data.clickProcessor.getKurtosis(), data.clickProcessor.getMean(),
                        data.clickProcessor.getSkewness(), data.clickProcessor.getVariance(), buffer,
                        data.clickProcessor.getZeros());
            }
        } else buffer-= buffer > 0 ? 2 : 0;
        long delta = timeStamp - lastArm;
        debug("kurtosis=%.4f std=%.4f avg=%.3f skew=%.3f variance=%.3f buffer=%.1f delta=%s zeros=%s",
                data.clickProcessor.getKurtosis(), data.clickProcessor.getStd(), data.clickProcessor.getMean(),
                data.clickProcessor.getSkewness(),
                data.clickProcessor.getVariance(),
                buffer, delta, data.clickProcessor.getZeros());
        lastArm = timeStamp;
    }
}
