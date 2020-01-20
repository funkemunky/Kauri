package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Cancellable;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "BadPackets (C)", description = "Checks for players sneaking and sprinting at the same time.",
        checkType = CheckType.BADPACKETS, punishVL = 20)
@Cancellable
public class BadPacketsC extends Check {

    @Packet
    public void onPlace(WrappedInFlyingPacket packet) {
        if(data.playerInfo.sprinting && data.playerInfo.sneaking && !data.lagInfo.lagging) {
            vl++;
            if(vl > 3) {
                flag("sprint=%1 sneak=%2 pos=%3",
                        data.playerInfo.sprinting,
                        data.playerInfo.sneaking,
                        packet.isPos());
            }
        }

        debug("sprint=" + data.playerInfo.sprinting
                + " sneak=" + data.playerInfo.sneaking + " pos=" + packet.isPos() + " vl=" + vl);
    }
}
