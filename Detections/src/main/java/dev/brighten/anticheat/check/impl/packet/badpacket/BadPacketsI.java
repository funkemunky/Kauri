package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInAbilitiesPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (I)", checkType = CheckType.BADPACKETS,
        executable = true,
        description = "Checks if the player is sending isFlying while not have allowedFlight.",
        punishVL = 1)
public class BadPacketsI extends Check {

    @Packet
    public void onFlying(WrappedInAbilitiesPacket packet) {
        if(packet.isFlying() && !packet.isAllowedFlight()
                && !packet.getPlayer().getAllowFlight() && !data.lagInfo.lagging) {
            vl+= 2;
            flag("isFlying=" + packet.isFlying() + " allowed=" + packet.isAllowedFlight());
        }
    }
}
