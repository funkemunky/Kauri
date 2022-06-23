package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (M)", description = "Checks for impossible pitch positions.",
        checkType = CheckType.BADPACKETS, punishVL = 1, executable = true)
public class BadPacketsM extends Check {

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet) {
        if(packet.hasRotationChanged() && Math.abs(packet.getLocation().getPitch()) > 90) {
            vl++;
            flag("pitch=%s", MathUtils.round(packet.getLocation().getPitch(), 2));
        }
    }
}
