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
    public void windowClick(WrappedInWindowClickPacket packet, long timeStamp) {
        if(data.playerInfo.deltaXZ > 0.1
                && !data.playerInfo.serverPos
                && timeStamp - data.playerInfo.lastVelocityTimestamp > 3000L
                && data.playerInfo.serverGround
                && !data.blockInfo.inLiquid
                && !data.blockInfo.onIce
                && !data.playerInfo.serverIsFlying) {
            if(vl++ > 4) {
                flag("clicked in window while moving");
            }
        } else vl-= vl > 0 ? 0.5 : 0;
    }
}
