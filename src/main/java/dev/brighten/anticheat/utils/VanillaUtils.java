package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class VanillaUtils {

    public static boolean isOnLadder(ObjectData data) {
        Location loc = new Location(data.getPlayer().getWorld(), data.playerInfo.to.x, data.box.minY, data.playerInfo.to.z);

        Block block = BlockUtils.getBlock(loc);

        if(block != null) {
            loc = null;
            return BlockUtils.isClimbableBlock(block);
        }
        loc = null;
        return false;
    }
}
