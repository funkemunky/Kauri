package dev.brighten.anticheat.check.impl.packets.badpackets;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.Packet;
import dev.brighten.anticheat.check.api.Setting;
import dev.brighten.api.check.CheckType;
import org.bukkit.GameMode;

@CheckInfo(name = "BadPackets (J)", description = "Checks for spoofing of creative mode.",
        checkType = CheckType.BADPACKETS, punishVL = 5, executable = false)
public class BadPacketsJ extends Check {

    @Setting(name = "defaultGamemode")
    private static String defaultGameMode = "SURVIVAL";

    @Packet
    public void onFlying(WrappedInFlyingPacket packet) {
        if(data.playerInfo.creative && !data.lagInfo.lagging
                && !data.playerInfo.serverPos
                && data.lagInfo.lastPacketDrop.hasPassed(1)
                && !data.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            vl++;
            flag("inCreative=%v gamemode=%v",
                    data.playerInfo.creative, data.getPlayer().getGameMode().name());
            RunUtils.task(() -> data.getPlayer().kickPlayer("Invalid gamemode"), Kauri.INSTANCE);
        }
        debug("inCreative=%v gamemode=%v", data.playerInfo.creative, data.getPlayer().getGameMode().name());
    }
}
