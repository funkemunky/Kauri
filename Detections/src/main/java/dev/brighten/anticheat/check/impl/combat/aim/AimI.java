package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Aim (I)", description = "Checks for weird pitch values that are near impossible to accomplish",
        checkType = CheckType.AIM, executable = true,
        punishVL = 12)
public class AimI extends Check {

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        final double pitch = data.playerInfo.to.pitch, lpitch = data.playerInfo.from.pitch;

        if(pitch == lpitch && pitch == 0 && !data.playerInfo.inVehicle
                && data.playerInfo.lastTeleportTimer.isPassed(1) && data.moveProcessor.deltaX > 12) {
            if(++buffer > 3) {
                vl++;
                flag("deltaX=%s buffer=%s", data.moveProcessor.deltaX, buffer);
            }
        } else buffer = 0;

        debug("pitch=%s lpitch=%s buffer=%s", pitch, lpitch, buffer);
    }
}
