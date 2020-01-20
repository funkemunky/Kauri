package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (A)", description = "Checks for blockDig and blockPlace times.",
        checkType = CheckType.BADPACKETS, punishVL = 12)
@Cancellable(cancelType = CancelType.INTERACT)
public class BadPacketsA extends Check {

    private long lastBlockPlace;

    @Packet
    public void onDig(WrappedInBlockDigPacket packet, long timeStamp) {
        if(timeStamp - lastBlockPlace < 5 && !data.lagInfo.lagging && data.lagInfo.lastPacketDrop.hasPassed(5)) {
            if(vl++ > 4) {
                flag("unblocked and blocked in same tick.");
            }
        } else vl-= vl > 0 ? 0.5 : 0;
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet, long timeStamp) {
        if(packet.getPlayer().getItemInHand() != null
                && BlockUtils.isTool(packet.getPlayer().getItemInHand())) lastBlockPlace = timeStamp;
    }
}
