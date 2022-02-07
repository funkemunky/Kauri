package dev.brighten.anticheat.check.impl.free.packet;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInHeldItemSlotPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.KauriVersion;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (C)", description = "Checks for players who send slot packets at the same time as flying.",
        checkType = CheckType.BADPACKETS, punishVL = 10, planVersion = KauriVersion.FREE, executable = true)
@Cancellable
public class BadPacketsC extends Check {

    private long lastFlying;

    @Packet
    public void use(WrappedInHeldItemSlotPacket packet, long current) {
        if(current - lastFlying < 10) {
            vl++;
            if(vl > 11) {
                flag("delta=%s", current - lastFlying);
            }
        } else if(vl > 0) vl--;
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long current) {
        if(data.playerInfo.lastTeleportTimer.isPassed(0))
            lastFlying = current;
    }
}
