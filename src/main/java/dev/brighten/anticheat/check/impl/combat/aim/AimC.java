package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "Aim (Type C)", description = "Checks for common denominators in yaw difference.")
public class AimC extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && data.playerInfo.deltaYaw > 0) {
            debug("deltaYaw=" + data.playerInfo.deltaYaw + " cinematicYaw=" + data.playerInfo.cinematicYaw);
        }
    }
}
