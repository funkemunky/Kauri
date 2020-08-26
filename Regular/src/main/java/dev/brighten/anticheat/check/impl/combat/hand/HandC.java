package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (C)", description = "Checks for impossible hit blocks.",
        checkType = CheckType.HAND, punishVL = 30)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandC extends Check {

    private long lastBlockDig;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        long delta = timeStamp - lastBlockDig;

        if(!data.lagInfo.lagging && data.lagInfo.lastPacketDrop.hasPassed(5) && delta < 10) {
            if(vl++ > 3) {
                flag("delta=%vms", delta);
            }
        } else vl-= vl > 0 ? 0.5f : 0;

        debug("delta=" + delta + "ms vl=" + vl);
    }

    @Packet
    public void onBlock(WrappedInBlockDigPacket packet, long timeStamp) {
        lastBlockDig = timeStamp;
    }
}
