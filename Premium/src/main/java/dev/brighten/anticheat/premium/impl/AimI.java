package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (I)", description = "Checks for weird pitch values that are near impossible to accomplish",
        punishVL = 12)
public class AimI extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        final double pitch = data.playerInfo.to.pitch, lpitch = data.playerInfo.from.pitch;

        if(pitch == lpitch && pitch == 0 && data.moveProcessor.deltaX > 12) {
            if(++buffer > 3) {
                vl++;
                flag("deltaX=%v buffer=%v", data.moveProcessor.deltaX, buffer);
            }
        } else buffer = 0;

        debug("pitch=%v lpitch=%v buffer=%v", pitch, lpitch, buffer);
    }
}
