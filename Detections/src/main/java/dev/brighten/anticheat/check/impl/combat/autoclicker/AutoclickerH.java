package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "Autoclicker (H)", description = "Checks for low standard deviation.",
        checkType = CheckType.AUTOCLICKER, punishVL = 6, vlToFlag = 2,
        devStage = DevStage.BETA)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerH extends Check {

    public float buffer;

    @Packet
    public void onArm(WrappedInArmAnimationPacket packet) {
        if(data.playerInfo.breakingBlock
                || data.playerInfo.lookingAtBlock
                || data.clickProcessor.isNotReady()
                || data.playerInfo.lastBrokenBlock.isNotPassed(5)
                || data.playerInfo.lastBlockDigPacket.isNotPassed(1)
                || data.playerInfo.lastBlockPlacePacket.isNotPassed(1))
            return;

        long range = data.clickProcessor.getMax() - data.clickProcessor.getMin();
        if(data.clickProcessor.getStd() < 0.3
                && data.clickProcessor.getMean() < 3
                && range > 3) {
            buffer++;
        } else if(buffer > 0) buffer-= 0.25f;

        debug("std=%.2f mean=%.1f range=%s buffer.1", data.clickProcessor.getStd(),
                data.clickProcessor.getMean(), range, buffer);
    }
}
