package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Event;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import org.bukkit.event.block.BlockPlaceEvent;

@CheckInfo(name = "Block (G)", description = "Checks for bad scaffold rotations", checkType = CheckType.BLOCK,
        devStage = DevStage.ALPHA)
public class BlockG extends Check {

    private int aimCount = 0, lastAimCount;
    private int buffer;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            aimCount++;
        }
    }

    @Event
    public void onBlockPlace(BlockPlaceEvent event) {

        if(Math.abs(aimCount - lastAimCount) <= 1 && aimCount >= 3) {
            if(++buffer > 3) {
               vl++;
               flag("a=%s,b=%s", aimCount, buffer);
            }
        } else buffer = 0;


        debug("aimCount=%s", aimCount);
        lastAimCount = aimCount;
        aimCount = 0;
    }
}
