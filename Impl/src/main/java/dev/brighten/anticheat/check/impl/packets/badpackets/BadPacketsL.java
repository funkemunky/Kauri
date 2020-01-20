package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (L)", description = "Checks for the spamming of block place packets.",
        checkType = CheckType.BADPACKETS, punishVL = 3)
public class BadPacketsL extends Check {

    private long flying, place;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        flying++;
        if(flying >= 10) {
            if(place > 200) {
                vl+= 4;
                flag("place=%1", place);
            }
            debug("place=%1", place);
            flying = place = 0;
        }
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        place++;
    }
}
