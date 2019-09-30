package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInAbilitiesPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutAbilitiesPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (D)",
        description = "Checks for clients spoofing flight permissions.",
        checkType = CheckType.BADPACKETS, punishVL = 10)
public class BadPacketsD extends Check {

    boolean serverAllowed, clientAllowed;

    @Packet
    public void server(WrappedOutAbilitiesPacket packet) {
        if(packet.isAllowedFlight()) {
            serverAllowed = true;
        } else if(!clientAllowed) {
            serverAllowed = false;
        }
    }

    @Packet
    public void client(WrappedInAbilitiesPacket packet) {
        if(packet.isAllowedFlight()) {
            clientAllowed = true;
        } else if(!serverAllowed) {
            clientAllowed = false;
        }
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet) {
        if(data.creation.hasNotPassed(8)) {
            serverAllowed = data.getPlayer().getAllowFlight();
        } else {
            if(!serverAllowed && clientAllowed) {
                if(vl++ > 1) {
                    flag("server=" + serverAllowed + " client=" + clientAllowed);
                }
            } else vl = 0;
        }
    }
}
