package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (Type B)", description = "Checks for common denominators in pitch difference.")
public class AimB extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && Math.abs(data.playerInfo.deltaPitch) > 1E-5 && Math.abs(data.playerInfo.to.pitch) < 78) {
            if(data.playerInfo.pitchGCD < 100000 && !data.playerInfo.cinematicModePitch && data.playerInfo.lastAttack.hasNotPassed(20)) {
                vl++;
                if(vl > 80) {
                    punish();
                } else if(vl > 20) {
                    flag("offset=" + data.playerInfo.pitchGCD + " deltaPitch=" + data.playerInfo.deltaPitch);
                }
            } else vl-= vl > 0 ? 0.5 : 0;
            debug("gcd=" + data.playerInfo.pitchGCD + " deltaPitch=" + data.playerInfo.deltaPitch + " vl=" + vl);
        }
    }
}
