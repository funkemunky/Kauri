package dev.brighten.anticheat.check.impl.packets.inventory;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Inventory (A)", description = "Checks if a user clicks in window with inventory open.",
        checkType = CheckType.INVENTORY, punishVL = 10)
public class InventoryA extends Check {

    @Packet
    public void 
}
