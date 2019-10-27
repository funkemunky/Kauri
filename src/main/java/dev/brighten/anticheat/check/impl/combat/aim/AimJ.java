package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (J)", description = "shit")
public class AimJ extends Check {

    private int ticks;
    @Packet
    public void flying(WrappedInFlyingPacket packet) {
        if(packet.isPos()) {
            debug("ticks=" + ticks);
            ticks = 0;
        } else ticks++;
    }
}
