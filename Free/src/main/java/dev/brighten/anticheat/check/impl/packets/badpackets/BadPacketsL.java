package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "BadPackets (L)", description = "Player sends block place packets without any item in hand",
        checkType = CheckType.BADPACKETS, developer = true, maxVersion = ProtocolVersion.V1_8_9)
public class BadPacketsL extends Check {

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        val pos = packet.getPosition();
        if(packet.getItemStack() == null
                || packet.getItemStack().getType().equals((XMaterial.AIR.parseMaterial()))
                && (pos == null || (pos.getX() == -1 && pos.getY() == -1 && pos.getZ() == -1))) {
            //TODO check if sends if player just right clicks block.
            vl++;
            flag("type=AIR");
        } else debug(packet.getItemStack().getType().name());
    }
}
