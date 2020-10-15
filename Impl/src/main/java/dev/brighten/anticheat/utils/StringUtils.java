package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Init
public class StringUtils {


    public static void sendMessage(CommandSender sender, TextComponent component) {
        if(sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(component);
        } else {
            sender.sendMessage(component.toLegacyText());
        }
    }

    public static ComponentBuilder start(String message) {
        return new ComponentBuilder(message);
    }

    public static void sendMessage(CommandSender sender, TextComponent component, Object... objects) {
        sendMessage(sender, TextComponent.fromLegacyText(String.format(component.toLegacyText(), objects)));
    }

    public static void sendMessage(CommandSender sender, String msg) {
        sendMessage(sender, TextComponent.fromLegacyText(Color.translate(msg)));
    }

    public static void sendMessage(CommandSender sender, BaseComponent... components) {
        sendMessage(sender, new TextComponent(components));
    }

    @AllArgsConstructor
    public enum Messages {

        LINE(genMsg("line", MiscUtils.line(Color.Dark_Gray))),
        PLAYER_ONLINE_NO(genMsg("player-not-online", "&cThe player provided is not online!")),
        PROVIDE_PLAYER(genMsg("provide-player", "&cYou must provide a player.")),
        DATA_ERROR(genMsg("data-error", "&cThere was an error trying to find your data.")),
        GC_COMPLETE(genMsg("gc-complete", "&aCompleted garbage collection in %.2fms!"));

        public Message msg;

        private static Message genMsg(String key, String msg) {
            return new Message(key, TextComponent.fromLegacyText(Color.translate(Kauri.INSTANCE
                    .msgHandler.getLanguage().msg(key, msg))));
        }

        public void send(CommandSender sender) {
            msg.send(sender);
        }

        public void send(CommandSender sender, Object... objects) {
            msg.send(sender, objects);
        }

        public static void reload() {
            for (Messages value : values()) {
                value.msg = genMsg(value.msg.name, value.msg.message.toLegacyText()
                        .replace("\u00A7", "&"));
            }
        }
    }

    public static class Message {
        public final String name;
        public final TextComponent message;

        public Message(String name, TextComponent message) {
            this.name = name;
            this.message = message;
        }

        public Message(String name, BaseComponent[] component) {
            this.name = name;
            this.message = new TextComponent(component);
        }

        public void send(CommandSender sender) {
            sendMessage(sender, message);
        }

        public void send(CommandSender sender, Object... objects) {
            sendMessage(sender, message, objects);
        }
    }
}
