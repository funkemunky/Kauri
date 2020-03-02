package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "test", checkType = CheckType.GENERAL)
public class Test extends Check {

    @Packet
    public void onPacket(WrappedInBlockPlacePacket place) {
        val pos = place.getPosition();
        debug("x=%1 y=%2 z=%3 itemName=%4", pos.getX(), pos.getY(), pos.getZ(),
                place.getItemStack().getType().name());
    }
}
