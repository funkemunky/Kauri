package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (I)", description = "Analysis of aim patterns.")
public class AimI extends Check {

    private boolean looked;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        looked = packet.isLook();
    }

    @Packet
    public void onPacket(WrappedInUseEntityPacket packet) {
        if(looked) {
            debug("vl=%v", ++vl);
        } else vl = 0;
    }
}
