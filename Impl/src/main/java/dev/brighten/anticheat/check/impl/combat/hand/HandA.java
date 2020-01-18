package dev.brighten.anticheat.check.impl.combat.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (A)", description = "Checks for irregular block place packets.",
        checkType = CheckType.HAND, punishVL = 12, developer = true, executable = false)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandA extends Check {

    private long lastFlying;

    @Packet
    public void onBlockPlace(WrappedInBlockPlacePacket place, long timeStamp) {
        long delta = timeStamp - lastFlying;

        if(!data.lagInfo.lagging && delta < 10) {
            if(vl++ > 6) {
                flag("delta=%1ms", delta);
            }
        } else vl-= vl > 0 ? 0.5f : 0;
        debug("delta=" + delta + "ms vl=" + vl);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        lastFlying = timeStamp;
    }
}
