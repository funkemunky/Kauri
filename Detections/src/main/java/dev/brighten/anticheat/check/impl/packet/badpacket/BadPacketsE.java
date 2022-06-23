package dev.brighten.anticheat.check.impl.packet.badpacket;

import cc.funkemunky.api.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;

@CheckInfo(name = "BadPackets (E)", description = "Looks for bad flying packets", checkType = CheckType.BADPACKETS,
        devStage = DevStage.ALPHA)
@Cancellable
public class BadPacketsE extends Check {

    private boolean lastGround;
    private int noPosTicks;

    @Packet
    public void onFlying(WrapperPlayClientPlayerFlying packet, long now) {
        if(!packet.hasPositionChanged()) {
            if(++noPosTicks > 1
                    && now - data.creation > 2000L
                    && data.playerInfo.lastTeleportTimer.isPassed(2)
                    && lastGround != packet.isOnGround()
                    && !data.playerInfo.serverGround
                    && !data.playerInfo.doingBlockUpdate) {
                vl++;
                flag("g=%s,%s", lastGround, packet.isOnGround());
            }
        } else {
            noPosTicks = 0;
        }

        lastGround = packet.isOnGround();
    }
}
