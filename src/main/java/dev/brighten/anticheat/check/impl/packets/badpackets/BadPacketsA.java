package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckType;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (A)", description = "Checks for blockDig and blockPlace times.",
        checkType = CheckType.BADPACKETS, punishVL = 12)
public class BadPacketsA extends Check {

    private long lastBlockPlace;

    @Packet
    public void onDig(WrappedInBlockDigPacket packet, long timeStamp) {
        if(timeStamp - lastBlockPlace < 5 && !data.lagInfo.lagging) {
            if(vl++ > 4) {
                flag("unblocked and blocked in same tick.");
            }
        } else vl-= vl > 0 ? 0.5 : 0;
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet, long timeStamp) {
        if(BlockUtils.isTool(packet.getItemStack())) lastBlockPlace = timeStamp;
    }
}
