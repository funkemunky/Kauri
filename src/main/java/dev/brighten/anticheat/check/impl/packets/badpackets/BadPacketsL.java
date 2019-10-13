package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (L)", description = "Checks for the spamming of block place packets.",
        checkType = CheckType.BADPACKETS, punishVL = 3)
public class BadPacketsL extends Check {

    private long lastPlace, ticks;

    @Packet
    public void onPlace(WrappedInBlockPlacePacket place, long timeStamp) {
        if(timeStamp - lastPlace > 1000L) {
            if(ticks > 500) {
                vl+=2;
                flag("ticks=" + ticks);
            }
            ticks = 0;
            lastPlace = timeStamp;
        } else ticks++;
    }
}
