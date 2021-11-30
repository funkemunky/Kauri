package dev.brighten.anticheat.check.impl.free.packet;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.math.IntVector;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

import java.util.Optional;

@CheckInfo(name = "BadPackets (L)", description = "Player sends block place packets without any item in hand",
        checkType = CheckType.BADPACKETS, devStage = DevStage.ALPHA, maxVersion = ProtocolVersion.V1_8_9,
        planVersion = KauriVersion.FREE, executable = true)
public class BadPacketsL extends Check {

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        IntVector pos = packet.getBlockPosition();
        if((packet.getItemStack() == null
                || (packet.getItemStack().getType().isBlock()
                && !packet.getItemStack().getType().equals(packet.getPlayer().getItemInHand().getType())))
                && (pos == null || (pos.getX() == -1 && pos.getY() == -1 && pos.getZ() == -1))) {
            //TODO check if sends if player just right clicks block.
            vl++;
            flag("p%s h=%s", Optional.ofNullable(packet.getItemStack()).map(i -> i.getType().name())
                    .orElse("NONE"), packet.getPlayer().getItemInHand().getType().name());
        } else if(packet.getItemStack() != null) debug(packet.getItemStack().getType().name());

        if(pos != null)
        debug("x=%s y=%s z=%s", pos.getX(), pos.getY(), pos.getZ());
    }
}
