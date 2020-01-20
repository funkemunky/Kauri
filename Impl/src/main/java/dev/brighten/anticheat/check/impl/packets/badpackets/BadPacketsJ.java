package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;

@CheckInfo(name = "BadPackets (J)", description = "Checks for spoofing of creative mode.",
        checkType = CheckType.BADPACKETS, punishVL = 5)
public class BadPacketsJ extends Check {

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.creative && !data.lagInfo.lagging && data.lagInfo.lastPacketDrop.hasPassed(1)
                && !data.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            vl++;
            flag("inCreative=%1 gamemode=%2",
                    data.playerInfo.creative, data.getPlayer().getGameMode().name());
        }
        debug("inCreative=%1 gamemode=%2", data.playerInfo.creative, data.getPlayer().getGameMode().name());
    }
}
