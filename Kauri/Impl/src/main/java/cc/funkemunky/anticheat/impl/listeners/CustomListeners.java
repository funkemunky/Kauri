package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInChatPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTabComplete;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Init
public class CustomListeners implements AtlasListener {

    private List<String> tabHider = Arrays.asList("ver", "version", "icanhasbukkit", "about", "kauri");

    @Message(name = "command.plugins.message")
    private static String message = "&fPlugins (%length%): &f{plugins}";

    @Message(name = "command.plugins.pluginFormat")
    private static String pluginFormat = "&a{plugin}&f, ";

    @ConfigSetting(path = "kauri.customize.hideKauri", name = "enabled")
    private boolean hideKauri = true;

    @ConfigSetting(path = "kauri.customize.hideAtlas", name = "enabled")
    private boolean hideAtlas = true;

    @ConfigSetting(path = "kauri.customize.hideKauri", name = "replace.enabled")
    private boolean replaceKauri = true;

    @ConfigSetting(path = "kauri.customize.hideKauri", name = "replace.string")
    private String replaceKauriString = "Anticheat";

    private String unknownCommand;

    public CustomListeners() {
        unknownCommand = Bukkit.spigot().getSpigotConfig().getString("messages.unknown-command");
    }

    @Listen(priority = ListenerPriority.HIGHEST)
    public void onEvent(PacketSendEvent event) {
        if((hideKauri || hideAtlas) && !event.getPlayer().hasPermission("kauri.command")) {
            if(event.getType().equals(Packet.Server.TAB_COMPLETE)) {
                WrappedOutTabComplete packet = new WrappedOutTabComplete(event.getPacket(), event.getPlayer());

                String[] results = packet.getResult();

                List<String> endResults = new ArrayList<>();

                for (int i = 0; i < results.length; i++) {
                    String result = results[i].toLowerCase();

                    if(hideKauri && result.contains("kauri")) {
                        if(replaceKauri) {
                            endResults.add(replaceKauriString);
                        }
                    } else if(!hideAtlas || !result.contains("atlas")) {
                        endResults.add(results[i]);
                    }
                }

                WrappedOutTabComplete toComplete = new WrappedOutTabComplete(endResults.toArray(new String[0]));

                event.setPacket(toComplete.getObject());
            }
        }
    }

    @Listen(priority = ListenerPriority.HIGHEST)
    public void onEvent(PacketReceiveEvent event) {
        if(true) {
            if(event.getType().equals(Packet.Client.CHAT)) {
                WrappedInChatPacket packet = new WrappedInChatPacket(event.getPacket(), event.getPlayer());

                if(packet.getMessage().startsWith("/") && packet.getMessage().length() > 1) {
                    String formatted = packet.getMessage().substring(1).toLowerCase();
                    switch(formatted) {
                        case "pl":
                        case "plugins": {
                            event.setCancelled(true);

                            String plugins = "";
                            int size = 0;
                            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                                if((plugin.getName().contains("Kauri") && !replaceKauri && hideKauri) || (plugin.getName().contains("Atlas") && hideAtlas)) continue;

                                plugins+= pluginFormat.replace("{plugin}", plugin.getName().contains("Kauri") && replaceKauri ? replaceKauriString : plugin.getName());
                                size++;
                            }

                            if(size > 0) {
                                plugins = plugins.substring(0, plugins.length() - 2);
                            }

                            event.getPlayer().sendMessage(Color.translate(message.replace("%length%", String.valueOf(size)).replace("{plugins}", plugins)));
                            break;
                        }
                        case "kauri":
                        case "alerts": {
                            if(hideKauri) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(unknownCommand);
                            }
                            break;
                        }
                        case "atlas": {
                            if(hideAtlas) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(unknownCommand);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
