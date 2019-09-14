package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.MiscUtils;

import java.util.HashSet;
import java.util.Set;

//TODO Test this with Vape and Vape Lite. Trying other things too.
//TODO Test for false positives with cinematic or check if cinematic causes the check to just not work.
@CheckInfo(name = "Aim (Type D)", description = "Designed to detect aimassists attempting to use cinematic smoothing.")
public class AimD extends Check {

    private long lastGCD;
    private boolean equal;
    private Set<Float> deltas = new HashSet<>();
    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(!packet.isLook()) return;

        long gcd = MiscUtils.gcd((long)data.playerInfo.pitchGCD, (long)data.playerInfo.lastPitchGCD);

        if(gcd == lastGCD && (gcd > 0 || data.playerInfo.deltaYaw > 2)) {
            vl++;
            equal = true;
        } else if(!equal) {
            vl = 0;
        } else {
            equal = false;
            vl-= vl > 0 ? 1 : 0;
        }

        if(data.playerInfo.cinematicModeYaw || data.playerInfo.cinematicModePitch) vl = 0;

        if(vl > 14) {
            punish();
        } else if(vl > 8) flag("pitch=0" + "g=" + gcd);

        lastGCD = gcd;

        debug("pitchDelta=" + MathUtils.round(data.playerInfo.deltaPitch, 2) + " gcd="
                + gcd + " vl=" + vl + " usingCinematic=" + data.playerInfo.cinematicModePitch);

       /*if(data.playerInfo.deltaPitch == 0 || data.playerInfo.cinematicModePitch) return;

        long gcd = MiscUtils.gcd((long)data.playerInfo.pitchGCD, (long)data.playerInfo.lastPitchGCD);
       if(ticks++ > 40) {

           if(deltas.size() < 30) {
               if(vl++ > 10) {
                   punish();
               } else if(vl > 5) {
                   flag("size=" + deltas.size());
               }
           } else vl = 0;
           debug("size=" + deltas.size() + " vl=" + vl);
           deltas.clear();
           ticks = 0;
       } else deltas.add(gcd);*/
    }
}
