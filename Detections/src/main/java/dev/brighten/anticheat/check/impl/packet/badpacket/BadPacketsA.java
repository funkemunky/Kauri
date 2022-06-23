package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import cc.funkemunky.api.utils.BlockUtils;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (A)", description = "Checks for blockDig and blockPlace times.",
        checkType = CheckType.BADPACKETS, punishVL = 12, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class BadPacketsA extends Check {

    private long lastBlockPlace;

    @Packet
    public void onDig(WrapperPlayClientPlayerDigging packet, long timeStamp) {
        if(timeStamp - lastBlockPlace < 5 && !data.lagInfo.lagging
                && data.lagInfo.lastPacketDrop.isPassed(5)) {
            if(vl++ > 4) {
                flag("unblocked and blocked in same tick.");
            }
        } else vl-= vl > 0 ? 0.5 : 0;
    }

    @Packet
    public void onPlace(WrapperPlayClientPlayerBlockPlacement packet, long timeStamp) {
        if(data.getPlayer().getItemInHand() != null
                && BlockUtils.isTool(data.getPlayer().getItemInHand())) lastBlockPlace = timeStamp;
    }
}
