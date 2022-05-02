package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (C)", description = "Checks for players who send slot packets at the same time as flying.",
        checkType = CheckType.BADPACKETS, punishVL = 10, executable = true)
@Cancellable
public class BadPacketsC extends Check {

    private long lastFlying;
    private int buffer;

    @Packet
    public void use(WrappedInHeldItemSlotPacket packet, long current) {
        if(current - lastFlying < 10 && data.lagInfo.lastPacketDrop.isPassed(2)) {
            if(++buffer > 11) {
                vl++;
                flag("delta=%s", current - lastFlying);
            }
        } else if(buffer > 0) buffer--;
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long current) {
        if(data.playerInfo.lastTeleportTimer.isPassed(0))
            lastFlying = current;
    }
}
