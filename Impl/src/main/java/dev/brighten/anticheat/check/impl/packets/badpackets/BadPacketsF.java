package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutAbilitiesPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (F)", description = "Checks if a client ignores sending a position packet.",
        checkType = CheckType.BADPACKETS, punishVL = 4, maxVersion = ProtocolVersion.V1_8_9)
public class BadPacketsF extends Check {

    private int packets;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            packets = 0;
        } else if(packets++ > 30) {
            vl++;
            flag("packets=%v", packets);
        }
    }

    @Packet
    public void onAbilities(WrappedOutAbilitiesPacket packet) {
        packets = 0;
    }
}
