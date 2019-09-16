package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;

@CheckInfo(name = "BadPackets (Type A)", description = "Checks for blockDig and blockPlace times.")
public class BadPacketsA extends Check {

    private long lastBlockPlace;

    @Packet
    public void onDig(WrappedInBlockDigPacket packet) {
        if(System.currentTimeMillis() - lastBlockPlace < 5) {
            if(vl++ > 12) {
                punish();
            } else if(vl > 4) {
                flag("unblocked and blocked in same tick.");
            }
        } else vl-= vl > 0 ? 0.5 : 0;
    }

    @Packet
    public void onPlace(WrappedInBlockPlacePacket packet) {
        if(BlockUtils.isTool(packet.getItemStack())) lastBlockPlace = System.currentTimeMillis();
    }
}
