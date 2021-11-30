package dev.brighten.anticheat.check.impl.free.packet;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckInfo(name = "BadPackets (F)", description = "Checks if the block placed is the item in the player's hand.",
        checkType = CheckType.BADPACKETS, punishVL = 2, planVersion = KauriVersion.FREE, executable = true)
public class BadPacketsF extends Check {

    @Event
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlockPlaced().getType();
        if(!type.isBlock()) return;

        boolean isNull = event.getItemInHand() == null;
        if(isNull || !event.getItemInHand().getType().equals(type)) {
            vl++;
            flag("blockType=%s itemStack=%s",  type.name(),
                    isNull ? "null" : event.getItemInHand().getType().name());
        }
    }
}
