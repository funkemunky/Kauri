package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import lombok.val;

@CheckInfo(name = "BadPackets (K)", description = "instant teleport", checkType = CheckType.BADPACKETS, punishVL = 1)
public class BadPacketsK extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet, long timeStamp) {
        if(packet.isPos()) {
            val dist = data.playerInfo.to.toVector().distance(data.playerInfo.from.toVector());
            if(timeStamp - data.playerInfo.lastServerPos > 200L
                    && dist > 50) {
                vl+=2;
                debug("distance=%1", dist);
            }
        }
    }
}
