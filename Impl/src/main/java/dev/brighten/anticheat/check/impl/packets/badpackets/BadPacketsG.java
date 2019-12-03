package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (G)", description = "Checks for long distance crashers.", checkType = CheckType.BADPACKETS, punishVL = 1)
public class BadPacketsG extends Check {

    @Packet
    public void onPacket(WrappedInFlyingPacket packet) {
        if(packet.isPos() && !data.playerInfo.serverPos) {
            if(Math.abs(packet.getY()) > Float.MAX_VALUE - 100) {
                vl+= 2;
                flag("y=" + packet.getY());
            }
        }
    }

}
