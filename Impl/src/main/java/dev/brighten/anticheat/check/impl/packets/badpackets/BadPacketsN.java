package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (N)", description = "Checks if the user is sending impossible movement packets.",
        developer = true,
        checkType = CheckType.BADPACKETS, maxVersion = ProtocolVersion.V1_8_9)
public class BadPacketsN extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long ts) {
        if(ts - data.creation < 4000L) return;

        if(Math.abs(data.playerInfo.deltaXZ) < 0.005 && data.playerInfo.deltaXZ != 0) {
            vl++;
            flag("type=xz deltaX=%v", data.playerInfo.deltaX);
        }

        if(Math.abs(data.playerInfo.deltaY) < 0.005 && data.playerInfo.deltaY != 0) {
            vl++;
            flag("type=y deltaY=%v", data.playerInfo.deltaY);
        }
    }
}
