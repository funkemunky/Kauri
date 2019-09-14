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
        if(!packet.isLook()) return;

        float accel = MathUtils.getDelta(data.playerInfo.deltaPitch, data.playerInfo.lDeltaPitch);

        if(accel < 1E-5 && (Math.abs(data.playerInfo.deltaPitch) > 0 || data.playerInfo.deltaYaw > 2)) {
            if(vl++ > 100) {
                punish();
            } else if(vl > 40) {
                flag("accel=" + accel + " deltaPitch=" + data.playerInfo.deltaPitch);
            }
        } else vl-= vl > 0 ? 6 : 0;
    }
}
