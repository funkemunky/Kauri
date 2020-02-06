package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (L)", description = "Player sends block place packets without any item in hand",
        checkType = CheckType.BADPACKETS)
public class BadPacketsL extends Check {

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        if(packet.getItemStack() == null
                || packet.getItemStack().getType().equals((XMaterial.AIR.parseMaterial()))) {
            //TODO check if sends if player just right clicks block.
            debug("received");
        } else debug(packet.getItemStack().getType().name());
    }
}
