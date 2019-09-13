package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;

//TODO Test this with Vape and Vape Lite. Trying other things too.
@CheckInfo(name = "Aim (Type D)", description = "Designed to detect aimassists attempting to use cinematic smoothing.")
public class AimD extends Check {

    private long lastGCD;
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        long gcd = MiscUtils.gcd((long)data.playerInfo.pitchGCD, (long)data.playerInfo.lastPitchGCD);

        if(gcd == lastGCD) {
            vl++;
        } else vl-= vl > 0 ? 1 : 0;

        if(data.playerInfo.cinematicModePitch || data.playerInfo.cinematicModeYaw) vl = 0;

        if(data.playerInfo.deltaPitch == 0 && vl > 12) {
            if(vl > 20) {
                punish();
            } else flag("pitch=0" + "g=" + gcd);
        }

        debug("pitchDelta=" + MathUtils.round(data.playerInfo.deltaPitch, 2) + " gcd="
                + gcd + " vl=" + vl + " usingCinematic=" + data.playerInfo.cinematicModePitch);

        lastGCD = gcd;
    }
}
