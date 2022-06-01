package dev.brighten.anticheat.listeners.generalChecks;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.listener.functions.PacketListener;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInChatPacket;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.api.KauriAPI;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;

@Init
public class ChatCheck {

    @ConfigSetting(path = "general.illegalCharacters", name = "enabled")
    private static boolean enabled = true;

    @ConfigSetting(path = "general.illegalCharacters", name = "whitelisted")
    private static List<Character> whitelistedCharacters = new ArrayList<>();

    @ConfigSetting(path = "general.illegalCharacters", name = "allowExtended")
    private static boolean allowExtendedCharacters = false;

    @ConfigSetting(path = "general.illegalCharacters", name = "messageLengthMax")
    private static int lengthMax = 250;

    private final PacketListener chatCheckListener = Atlas.getInstance().getPacketProcessor()
            .process(Kauri.INSTANCE, EventPriority.HIGHEST, event -> {
                if(!enabled) return;
                //Packet exemption check
                if(KauriAPI.INSTANCE.getPacketExemptedPlayers().contains(event.getPlayer().getUniqueId())) return;

                WrappedInChatPacket packet = new WrappedInChatPacket(event.getPacket(), event.getPlayer());

                if(packet.getMessage().length() <= 0) return;

                if(packet.getMessage().length() > lengthMax) {
                    event.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("msg-too-long",
                                    "&8[&6K&8] &cYour chat message was cancelled because it was too long."));
                    event.setCancelled(true);
                }

                int min = 0;
                int max = allowExtendedCharacters ? 591 : 255;

                for (char c : packet.getMessage().toCharArray()) {
                    if((int)c > min && (int)c < max || whitelistedCharacters.contains(c)) continue;

                    event.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("illegal-chars",
                                    "&8[&6K&8] &cYou are not allowed to use character \"" + c + "\"."));
                    event.setCancelled(true);
                }
            }, Packet.Client.CHAT);
}
