package dev.brighten.anticheat.check.impl.combat.aim;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.Bukkit;

@CheckInfo(name = "Aim (I)", description = "Checks for bad math from aimbots.",checkType = CheckType.AIM)
public class AimI extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook()) {
            if(data.playerInfo.pitchGCD > 4E7) {
                if(vl++ > 6) {
                    flag("");
                }
            } else vl-= vl > 0 ? 0.2 : 0;
        }
    }
}
