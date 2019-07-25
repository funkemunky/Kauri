package cc.funkemunky.anticheat.impl.checks.player.inventory;

import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import org.bukkit.event.Event;

@CheckInfo(name = "Inventory (Type A)", description = "Checks if a player's inventory is open illegitimately.", type = CheckType.INVENTORY)
public class InventoryA extends Check {
    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {

    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
