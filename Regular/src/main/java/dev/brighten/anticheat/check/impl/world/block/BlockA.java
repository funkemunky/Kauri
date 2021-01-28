package dev.brighten.anticheat.check.impl.world.block;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.Location;

@CheckInfo(name = "Block (A)", description = "Checks for impossible scaffold sprinting.", developer = true,
        checkType = CheckType.BLOCK)
public class BlockA extends Check {

    @Packet
    public void onBlock(WrappedInBlockPlacePacket event) {
        Location placeLoc = new Location(event.getPlayer().getWorld(), event.getPosition().getX(),
                event.getPosition().getY(), event.getPosition().getZ());

        //Getting place block
        placeLoc.add(event.getFace().getAdjacentX(), event.getFace().getAdjacentY(), event.getFace().getAdjacentZ());

        debug("x=%v y=%v z=%v", placeLoc.getBlockX(), placeLoc.getBlockY(), placeLoc.getBlockZ());
    }
}
