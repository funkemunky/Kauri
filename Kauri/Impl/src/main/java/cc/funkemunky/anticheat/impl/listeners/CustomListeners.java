package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInChatPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTabComplete;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@Init
public class CustomListeners implements AtlasListener {

    private String unknownCommand;

    public static boolean isAllowed = false;

    public CustomListeners() {
        unknownCommand = Bukkit.spigot().getConfig().getString("messages.unknown-command");

        isAllowed = MenuUtils.isGUIAllowed();
    }

    @Listen(priority = ListenerPriority.HIGHEST)
    public void onEvent(PacketSendEvent event) {
        if(!isAllowed) return;
        if((ImportantListeners.hideKauri || ImportantListeners.hideAtlas) && !event.getPlayer().hasPermission("kauri.command")) {
            if(event.getType().equals(Packet.Server.TAB_COMPLETE)) {
                WrappedOutTabComplete packet = new WrappedOutTabComplete(event.getPacket(), event.getPlayer());

                String[] results = packet.getResult();

                List<String> endResults = new ArrayList<>();

                for (int i = 0; i < results.length; i++) {
                    String result = results[i].toLowerCase();

                    if(ImportantListeners.hideKauri && result.contains("kauri")) {
                        if(ImportantListeners.replaceKauri && results.length > 4) {
                            endResults.add(ImportantListeners.replaceKauriString);
                        }
                    } else if(!ImportantListeners.hideAtlas || !result.contains("atlas")) {
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
        if(!isAllowed) return;
        if(event.getType().equals(Packet.Client.CHAT)) {
            WrappedInChatPacket packet = new WrappedInChatPacket(event.getPacket(), event.getPlayer());
            if (!event.getPlayer().hasPermission("kauri.command")) {

                if (packet.getMessage().startsWith("/") && packet.getMessage().length() > 1) {
                    String formatted = packet.getMessage().substring(1).toLowerCase();

                    if (formatted.split(" ")[0].equals(ImportantListeners.replaceKauriString.toLowerCase()) || formatted.split(" ")[0].equals(ImportantListeners.mainCommand)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(Color.Red + "No permission.");
                        return;
                    }
                    switch (formatted.split(" ")[0]) {
                        case "pl":
                        case "plugins": {
                            if (ImportantListeners.pluginsEnabled) {
                                event.setCancelled(true);

                                String plugins = "";
                                int size = 0;
                                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                                    if ((plugin.getName().contains("Kauri") && !ImportantListeners.replaceKauri && ImportantListeners.hideKauri) || (plugin.getName().contains("Atlas") && ImportantListeners.hideAtlas))
                                        continue;

                                    plugins += ImportantListeners.pluginFormat.replace("{plugin}", plugin.getName().contains("Kauri") && ImportantListeners.replaceKauri ? ImportantListeners.replaceKauriString : plugin.getName());
                                    size++;
                                }

                                if (size > 0) {
                                    plugins = plugins.substring(0, plugins.length() - 2);
                                }

                                event.getPlayer().sendMessage(Color.translate(ImportantListeners.message.replace("%length%", String.valueOf(size)).replace("{plugins}", plugins)));
                            }
                            break;
                        }
                        case "kauri": {
                            if (ImportantListeners.hideKauri) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(unknownCommand);
                            }
                            break;
                        }
                        case "anticheat": {
                            if (ImportantListeners.hideKauri) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(Color.Red + "No permission.");
                            }
                            break;
                        }
                        case "atlas": {
                            if (ImportantListeners.hideAtlas) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(unknownCommand);
                            }
                            break;
                        }
                        case "?":
                        case "help": {
                            val split = packet.getMessage().split(" ");

                            if (split.length > 1) {
                                String message = packet.getMessage().replace(split[0] + " ", "");
                                if ((ImportantListeners.hideKauri && message.toLowerCase().contains("kauri")) || (ImportantListeners.hideAtlas && message.toLowerCase().contains("atlas"))) {
                                    event.setCancelled(true);
                                    event.getPlayer().sendMessage(Color.Red + "No help for " + message);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
