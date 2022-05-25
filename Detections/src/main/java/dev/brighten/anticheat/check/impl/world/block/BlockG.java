package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckInfo(name = "Block (G)", description = "Checks for bad scaffold rotations", checkType = CheckType.BLOCK,
        devStage = DevStage.ALPHA)
public class BlockG extends Check {

    private int aimCount = 0, lastAimCount;
    private float rotChange;
    private int buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            aimCount++;
            rotChange+= Math.abs(data.playerInfo.deltaYaw) + Math.abs(data.playerInfo.deltaPitch);
        }
    }

    @Event
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockFace face = event.getBlockPlaced().getFace(event.getBlockAgainst());
        if((Math.abs(aimCount - lastAimCount) <= 1 && aimCount > 2 && rotChange > 100)
                || (aimCount <= 5 && rotChange > 300
                && event.getPlayer().getLocation().distanceSquared(event.getBlockPlaced().getLocation()) <= 4
                && event.getPlayer().getLocation().getY() > event.getBlockPlaced().getY() + 0.8)) {
            if(++buffer > 3) {
               vl++;
               flag("a=%s,r=%.2f,b=%s", aimCount, rotChange, buffer);
            }
        } else buffer = 0;



        debug("aimCount=%s rot=%.2f", aimCount, rotChange);
        lastAimCount = aimCount;
        aimCount = 0;
        rotChange = 0;
    }
}
