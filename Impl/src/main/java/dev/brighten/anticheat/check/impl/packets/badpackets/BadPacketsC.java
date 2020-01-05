package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "BadPackets (C)", description = "Checks for players sneaking and sprinting at the same time.",
        checkType = CheckType.BADPACKETS, punishVL = 20)
public class BadPacketsC extends Check {

    @Packet
    public void onPlace(WrappedInFlyingPacket packet) {
        if(data.playerInfo.sprinting && data.playerInfo.sneaking) {
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
