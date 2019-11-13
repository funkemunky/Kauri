package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Hand (C)", description = "Checks for impossible hit blocks.",
        checkType = CheckType.HAND, punishVL = 10)
public class HandC extends Check {

    private long lastBlockDig;

    @Packet
    public void onUse(WrappedInUseEntityPacket packet, long timeStamp) {
        long delta = timeStamp - lastBlockDig;

        if(!data.lagInfo.lagging && delta < 10) {
            if(vl++ > 3) {
                flag("delta=" + delta + "ms");
            }
        } else vl-= vl > 0 ? 0.5f : 0;

        debug("delta=" + delta + "ms vl=" + vl);
    }

    @Packet
    public void onBlock(WrappedInBlockDigPacket packet, long timeStamp) {
        lastBlockDig = timeStamp;
    }
}
