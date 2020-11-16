package dev.brighten.anticheat.check.impl.combat.killaura;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Killaura (G)", description = "Checks for weird packet screw ups with killauras",
        checkType = CheckType.KILLAURA, developer = true)
public class KillauraG extends Check {

    private int ticks;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isPos()) return;

        ticks++;
    }

    @Packet
    public void onUse(WrappedInUseEntityPacket packet) {
        debug("ticks=%v", ticks);
        ticks = 0;
    }
}
