package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutRelativePosition;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (N)", description = "Debugging shit")
public class BadPacketsN extends Check {

    @Packet
    public void onOut(WrappedOutRelativePosition packet) {
        debug("entity=%v x=%v y=%v z=%v yaw=%v pitch=%v", packet.getId(),
                packet.getX() / 32d, packet.getY() / 32d, packet.getZ() / 32d, packet.getYaw(), packet.getPitch());
    }
}
