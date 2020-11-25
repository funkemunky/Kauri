package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (M)", description = "Checks for impossible pitch positions.",
        checkType = CheckType.BADPACKETS, punishVL = 1, planVersion = KauriVersion.FREE)
public class BadPacketsM extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(packet.isLook() && Math.abs(packet.getPitch()) > 90) {
            vl++;
            flag("pitch=%v", MathUtils.round(packet.getPitch(), 2));
        }
    }
}
