package dev.brighten.anticheat.premium.impl;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.Color;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.Verbose;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "Aim (G)", description = "A simple check to detect Vape's aimassist.",
        checkType = CheckType.AIM, developer = true, punishVL = 30)
public class AimG extends Check {

    private Verbose verbose = new Verbose(50, 6);
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        val deltaX = Math.abs(data.moveProcessor.deltaX - data.moveProcessor.lastDeltaX);
        val deltaY = Math.abs(data.moveProcessor.deltaY - data.moveProcessor.lastDeltaY);

        if(deltaY <= 2 && deltaX > 0 && deltaX <= 6 && data.moveProcessor.deltaX > 15) {
            if(verbose.flag(1, 7)) {
                vl++;
                flag("deltaX=%1 deltaY=%2 vb=%3", deltaX, deltaY, verbose.value());
            }
            debug(Color.Green + "Flag");
        } else debug("deltaX=%1 deltaY=%2", deltaX, deltaY);
    }
}
