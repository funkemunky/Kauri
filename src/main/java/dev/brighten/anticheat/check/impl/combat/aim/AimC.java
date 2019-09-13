package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (Type C)", description = "Checks for common denominators in yaw difference.")
public class AimC extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.playerInfo.deltaYaw > 0) {
            if(!data.playerInfo.cinematicModeYaw) {
                if(data.playerInfo.yawGCD < 1E6) {
                    if((vl+= (data.playerInfo.deltaYaw < 2 ? 2 : 1)) > 150) {
                        punish();
                    } else if(vl > 80) {
                        flag("yaw=" + data.playerInfo.deltaYaw + " g=" + data.playerInfo.yawGCD);
                    }
                } else vl-= vl > 0 ? (data.playerInfo.deltaYaw > 10 ? 20 : (data.playerInfo.deltaYaw > 1 ? 5 : 2)) : 0;
                debug("gcd=" + data.playerInfo.yawGCD
                        + " yawDelta=" + MathUtils.round(data.playerInfo.deltaYaw, 3) + " vl=" + vl);
            } else vl-= vl > 0 ? 0.25 : 0;
        }
    }
}
