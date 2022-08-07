package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "BadPackets (E)", description = "Looks for bad flying packets", checkType = CheckType.BADPACKETS,
        devStage = DevStage.ALPHA)
@Cancellable
public class BadPacketsE extends Check {

    private boolean lastGround;
    private int noPosTicks;

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long now) {
        if(!packet.isPos()) {
            if(++noPosTicks > 1
                    && now - data.creation > 2000L
                    && data.playerInfo.lastTeleportTimer.isPassed(2)
                    && lastGround != packet.isGround()
                    && data.playerInfo.vehicleTimer.isNotPassed(2)
                    && !data.playerInfo.serverGround
                    && !data.playerInfo.doingBlockUpdate) {
                vl++;
                flag("g=%s,%s", lastGround, packet.isGround());
            }
        } else {
            noPosTicks = 0;
        }

        lastGround = packet.isGround();
    }
}
