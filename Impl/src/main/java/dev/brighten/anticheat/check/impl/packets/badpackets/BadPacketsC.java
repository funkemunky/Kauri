package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import cc.funkemunky.api.utils.math.cond.MaxDouble;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (C)", description = "Checks for players who send slot packets at the same time as flying.",
        checkType = CheckType.BADPACKETS, punishVL = 20)
@Cancellable
public class BadPacketsC extends Check {

    private long lastTimestamp;
    private MaxDouble verbose = new MaxDouble(20);

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        lastTimestamp = timeStamp;
    }

    @Packet
    public void onHeld(WrappedInHeldItemSlotPacket packet, long timeStamp) {
        long delta = timeStamp - lastTimestamp;

        if(delta <= 8 && data.lagInfo.lastPacketDrop.hasPassed(5)) {
            if(verbose.add() > 7) {
                vl++;
                flag("delta=%v ping=%p", delta);
            }
        } else verbose.subtract(0.5);
    }
}
