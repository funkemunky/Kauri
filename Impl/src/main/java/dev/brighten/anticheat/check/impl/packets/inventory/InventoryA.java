package dev.brighten.anticheat.check.impl.packets.inventory;

import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (A)", description = "Checks if user clicks while window is open.",
        checkType = CheckType.INVENTORY, punishVL = 10, cancellable = true)
@Cancellable(cancelType = CancelType.INVENTORY)
public class InventoryA extends Check {

    @Packet
    public void onWindow(WrappedInWindowClickPacket packet) {
        if(packet.getId() == 0 && !data.playerInfo.inventoryOpen) {
            vl++;
            if(vl > 2) flag("id=%1", packet.getId());
        }
    }
}
