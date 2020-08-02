package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (A)", description = "Checks if user clicks while window is open.",
        checkType = CheckType.INVENTORY, punishVL = 10, cancellable = true, developer = true)
@Cancellable(cancelType = CancelType.INVENTORY)
public class InventoryA extends Check {

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        debug("action=%v button=%v counter=%v id=%v slot=%v mode=%v",
                packet.getAction().name(), packet.getButton(), packet.getCounter(),
                packet.getId(), packet.getSlot(), packet.getMode());
    }
}
