package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (B)", description = "Checks for irregular block place packets.",
        checkType = CheckType.HAND, punishVL = 12, developer = true, executable = false)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandB extends Check {

    private long lastFlying;

    @Packet
    public void onDig(WrappedInBlockDigPacket packet, long timeStamp) {
        long delta = timeStamp - lastFlying;

        if(!data.lagInfo.lagging && data.lagInfo.lastPacketDrop.hasPassed(5) && delta < 10) {
            if(vl++ > 6) {
                flag("delta=%1ms action=%2", delta, packet.getAction().name());
            }
        } else vl-= vl > 0 ? 1f : 0;

        debug("delta=" + delta  + "ms action=" + packet.getAction().name() + " vl=" + vl);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        lastFlying = timeStamp;
    }
}
