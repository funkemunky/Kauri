package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (A)", description = "Checks for invalid inventory clicks",
        checkType = CheckType.INVENTORY, developer = true)
public class InventoryA extends Check {

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        debug("name=%v button=%v id=%v mode=%v slot=%v counter=%v",
                packet.getAction().name(), packet.getButton(), packet.getId(), packet.getMode(),
                packet.getSlot(), packet.getCounter());
    }
}
