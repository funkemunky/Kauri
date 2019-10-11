package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (I)", description = "Checks for clicking in inventory while moving.",
        checkType = CheckType.BADPACKETS)
public class BadPacketsI extends Check {

    @Packet
    public void windowClick(WrappedInWindowClickPacket packet) {
        if(data.playerInfo.deltaXZ > 0.1
                && data.playerInfo.serverGround
                && !data.blockInfo.inLiquid
                && !data.blockInfo.onIce
                && !data.playerInfo.isFlying) {
            flag("dropped item while moving");
        }
    }
}
