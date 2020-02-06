package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (K)", description = "Checks for duplicate held item packets",
        punishVL = 1, checkType = CheckType.BADPACKETS)
public class BadPacketsK extends Check {

    private int lastSlot = -1;

    //TODO Check if a server sends packet of duplicate a client would respond.
    @Packet
    public void onHeld(WrappedInHeldItemSlotPacket packet) {
        if(lastSlot != -1 && lastSlot == packet.getSlot()) {
            vl++;
            flag("current=%1;last=%2", packet.getSlot(), lastSlot);
        }
        lastSlot = packet.getSlot();
    }
}
