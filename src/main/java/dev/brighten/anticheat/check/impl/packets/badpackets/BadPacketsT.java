package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Test", checkType = CheckType.BADPACKETS, developer = true)
public class BadPacketsT extends Check {

    private long lastTimestamp;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        debug("delta=" + delta + "ms ping=" + data.lagInfo.transPing + " lagging=" + data.lagInfo.lagging);
        lastTimestamp = timeStamp;
    }
}
