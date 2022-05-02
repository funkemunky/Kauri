package dev.brighten.anticheat.check.impl.world.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (C)", description = "Checks for impossible hit blocks.",
        checkType = CheckType.HAND, punishVL = 5, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandC extends Check {

    private long lastBlockDig;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        long delta = timeStamp - lastBlockDig;

        if(!data.lagInfo.lagging && data.lagInfo.lastPacketDrop.isPassed(5) && delta < 10) {
            if(vl++ > 3) {
                flag("delta=%sms", delta);
            }
        } else vl-= vl > 0 ? 0.5f : 0;

        debug("delta=" + delta + "ms vl=" + vl);
    }

    @Packet
    public void onBlock(WrappedInBlockDigPacket packet, long timeStamp) {
        lastBlockDig = timeStamp;
    }
}
