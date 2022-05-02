package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (K)", description = "Checks for duplicate held item packets",
        punishVL = 1, checkType = CheckType.BADPACKETS, executable = true)
public class BadPacketsK extends Check {

    private int lastSlot = -1, buffer;

    @Packet
    public void onHeld(WrappedInHeldItemSlotPacket packet) {
        if(lastSlot != -1 && lastSlot == packet.getSlot() && data.lagInfo.lastPacketDrop.isPassed(2)) {
            if(++buffer > 3) {
                vl++;
                flag("current=%s;last=%s", packet.getSlot(), lastSlot);
            }
        } else if(buffer > 0) buffer--;
        debug("slot=%s lastslot=%s", packet.getSlot(), lastSlot);
        lastSlot = packet.getSlot();
    }
}
