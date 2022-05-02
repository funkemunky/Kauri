package dev.brighten.anticheat.check.impl.world.block;

import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckInfo(name = "Block (D)", checkType = CheckType.BLOCK, description = "Unusual block placement", punishVL = 6)
@Cancellable(cancelType = CancelType.PLACE)
public class BlockD extends Check {

    @Event
    public void onBlock(BlockPlaceEvent event) {
        Block ba = event.getBlockAgainst();

        if (!event.getBlockPlaced().getType().isBlock()) return;
        Block b = event.getBlock();
        double ypos = b.getLocation().getY() - data.getPlayer().getLocation().getY();
        double distance = data.getPlayer().getLocation().distance(b.getLocation());
        double ab_distance = data.getPlayer().getLocation().distance(ba.getLocation()) + 0.3;

        if (distance >= 1.4 && distance > ab_distance && ypos <= 0.5) {
            vl++;
            flag(300, "d:%.4f, ad:%.4f y=%.1f", distance, ab_distance, ypos);
        }
    }
}
