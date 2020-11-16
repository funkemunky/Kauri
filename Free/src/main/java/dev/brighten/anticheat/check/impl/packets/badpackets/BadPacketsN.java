package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (N)", description = "Checks for improper y setting.",
        developer = true, checkType = CheckType.BADPACKETS, planVersion = KauriVersion.FREE)
public class BadPacketsN extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        double predicted = getY(data.playerInfo.deltaY);

        if(predicted < 0) return;

        double delta = Math.abs(data.playerInfo.deltaY - predicted);
        if(data.playerInfo.deltaY > predicted && delta > 1E-14
                && data.playerInfo.liquidTimer.isPassed(20)
                && data.playerInfo.slimeTimer.isPassed(8)
                && !data.playerInfo.flightCancel) {
            vl++;
            flag("delta=%v y=%v", delta, data.playerInfo.deltaY);
        }
        debug("%v, %v, %v", data.playerInfo.deltaY, predicted, delta);
    }

    private double getY(double deltaY) {
        double currentPredictor = data.playerInfo.jumpHeight;

        while(Math.abs(currentPredictor - deltaY) > 0.065 && currentPredictor > 0) {
            currentPredictor-= 0.08;
            currentPredictor*= 0.98f;
        }

        return currentPredictor;
    }
}
