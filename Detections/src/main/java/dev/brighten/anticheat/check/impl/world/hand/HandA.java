package dev.brighten.anticheat.check.impl.world.hand;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Hand (A)", description = "Checks for irregular block place packets.",
        checkType = CheckType.HAND, punishVL = 4, executable = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class HandA extends Check {

    private long lastFlying;
    private int buffer;

    @Packet
    public void use(WrappedInBlockPlacePacket packet, long current) {
        long delta = current - lastFlying;
        if(delta < 10 && data.lagInfo.lastPacketDrop.isPassed(1)) {
            if(++buffer > 7) {
                vl++;
                flag("delta=%s", delta);
            }
        } else if(buffer > 0) buffer--;

        debug("delta=%sms buffer=%s", delta, buffer);
    }

    @Packet
    public void flying(WrappedInFlyingPacket packet, long current) {
        if(data.playerInfo.lastTeleportTimer.isPassed(0) && !data.excuseNextFlying)
            lastFlying = current;
    }
}
