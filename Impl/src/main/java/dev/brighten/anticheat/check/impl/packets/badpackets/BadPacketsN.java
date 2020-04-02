package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (N)", description = "Checks for dumb shiz", developer = true,
        checkType = CheckType.BADPACKETS)
public class BadPacketsN extends Check {

    private long lastPing;
    private TickTimer lastPingChange = new TickTimer(40);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long ts) {
        if(ts - data.creation < 4000L) return;
        if(data.lagInfo.ping == 0 && data.lagInfo.transPing > 1) {
            vl++;
            flag("t=1");
        }

        if(data.lagInfo.ping != lastPing) lastPingChange.reset();

        if(lastPingChange.hasPassed(80)) {
            vl++;
            flag("t=2;p=%v", lastPingChange.getPassed());
        }

        lastPing = data.lagInfo.ping;
    }
}
