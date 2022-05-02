package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (G)", description = "Looks for impossible dropping of items",
        checkType = CheckType.BADPACKETS,
        punishVL = 10, executable = true)
public class BadPacketsG extends Check {

    private long lastItemDrop;
    private int verbose;
    @Packet
    public void onPacket(WrappedInBlockDigPacket packet, long timeStamp) {
        if(packet.getAction().equals(WrappedInBlockDigPacket.EnumPlayerDigType.DROP_ITEM)) {
            long delta = timeStamp - lastItemDrop;

            if(delta < 35) {
                if(verbose++ > 5) {
                    vl++;
                    flag("delta=" + delta);
                }
            } else verbose-= verbose > 0 ? 1 : 0;
            lastItemDrop = timeStamp;
        }
    }
}
