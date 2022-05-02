package dev.brighten.anticheat.check.impl.world.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (B)", description = "Checks for irregular block dig packets.",
        checkType = CheckType.HAND, punishVL = 5, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandB extends Check {

    private long lastFlying;

    @Packet
    public void onDig(WrappedInBlockDigPacket packet, long timeStamp) {
        long delta = timeStamp - lastFlying;

        if(!data.lagInfo.lagging && data.lagInfo.lastPacketDrop.isPassed(5) && delta < 10) {
            if(vl++ > 6) {
                flag("delta=%sms action=%s", delta, packet.getAction().name());
            }
        } else vl-= vl > 0 ? 1f : 0;

        debug("delta=" + delta  + "ms action=" + packet.getAction().name() + " vl=" + vl);
    }

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long current) {
        if(data.playerInfo.lastTeleportTimer.isPassed(0))
            lastFlying = current;
    }
}
