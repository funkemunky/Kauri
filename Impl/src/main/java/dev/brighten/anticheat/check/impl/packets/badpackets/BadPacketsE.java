package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (E)", description = "Detects when clients sent use packets at same time as flying packets.",
        checkType = CheckType.BADPACKETS, punishVL = 40)
public class BadPacketsE extends Check {

    private long lastFlying;

    @Packet
    public void use(WrappedInUseEntityPacket packet, long timeStamp) {
        long delta = timeStamp - lastFlying;
        if(delta < 2) {
            if(!data.lagInfo.lagging) {
                if(vl++ > 15) {
                    flag("delta=" + delta);
                }
            } vl-= vl > 0 ? 0.1 : 0;
            debug("lagging=" + data.lagInfo.lagging + " vl=" + vl + " delta=" + delta);
        } else vl-= vl > 0 ? 1 : 0;
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long timeStamp) {
        lastFlying = timeStamp;
    }
}
