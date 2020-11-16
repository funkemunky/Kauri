package dev.brighten.anticheat.check.impl.packets.badpackets;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckInfo(name = "BadPackets (F)", description = "Checks if the block placed is the item in the player's hand.",
        checkType = CheckType.BADPACKETS, punishVL = 2, planVersion = KauriVersion.FREE)
public class BadPacketsF extends Check {

    @Event
    public void onBlockPlace(BlockPlaceEvent event) {
        if(!event.getBlockPlaced().getType().isBlock()) return;

        boolean isNull = event.getItemInHand() == null;
        if(isNull || !event.getItemInHand().getType().equals(event.getBlockPlaced().getType())) {
            vl++;
            flag("blockType=%v itemStack=%v",  event.getBlockPlaced().getType().name(),
                    isNull ? "null" : event.getItemInHand().getType().name());
        }
    }
}
