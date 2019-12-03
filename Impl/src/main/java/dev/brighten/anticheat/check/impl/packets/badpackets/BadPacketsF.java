package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (F)", description = "Checks for boxer crashers.", checkType = CheckType.BADPACKETS, punishVL = 60)
public class BadPacketsF extends Check {

    private long lastHeldItem;
    @Packet
    public void onPacket(WrappedInHeldItemSlotPacket wrapper, long timeStamp) {

        long delta = timeStamp - lastHeldItem;

        if(delta < 5) {
            if(vl++ > 50) {
                flag("delta=" + delta);
            }
        } else vl-= vl > 0 ? 1 : 0;
        lastHeldItem = timeStamp;
    }
}
