package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (N)", description = "Checks for impossible positions.",
        checkType = CheckType.BADPACKETS, punishVL = 4, developer = true, enabled = false)
public class BadPacketsN extends Check {

    private boolean lastNonFlying;
    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            if(data.playerInfo.to.toVector().distance(data.playerInfo.from.toVector()) == 0
                    && timeStamp - data.creation > 1000L
                    && data.playerInfo.lastRespawnTimer.hasPassed(20)
                    && timeStamp - data.playerInfo.lastServerPos > 100L
                    && !lastNonFlying) {
                vl++;
                flag("");
            }
            lastNonFlying = false;
        } else {
            lastNonFlying = true;
        }
    }

}
