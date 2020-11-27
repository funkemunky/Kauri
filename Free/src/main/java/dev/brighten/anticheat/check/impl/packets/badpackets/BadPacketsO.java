package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "BadPackets (O)", description = "Designed to patch disablers for Kauri.",
        checkType = CheckType.BADPACKETS, developer = true, punishVL = 10, vlToFlag = 0)
public class BadPacketsO extends Check {

    @Setting(name  = "kickPlayer")
    private static boolean kickPlayer = true;

    private int buffer;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long current) {
        val response = Kauri.INSTANCE.keepaliveProcessor.getResponse(data);

        final boolean present = response.isPresent();
        final int responseTime = present ? Kauri.INSTANCE.keepaliveProcessor.tick - response.get().start : -1;
        if((!present
                || responseTime > 50)
                && current - data.creation > 2000L) {
            if(++buffer > 12) {
                vl++;
                flag(!present ? "t=not present" : "t=response s=" + responseTime);
                if(kickPlayer && vl > 5) {
                    RunUtils.task(() -> data.getPlayer().kickPlayer("Invalid packet"));
                    vl = 0;
                }
            }
        }
    }
}
