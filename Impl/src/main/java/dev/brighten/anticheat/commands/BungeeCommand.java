package dev.brighten.anticheat.commands;

import cc.funkemunky.api.bungee.BungeeAPI;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

@Init
public class BungeeCommand extends BaseCommand {

    @ConfigSetting(path = "general", name = "bungeeCmd")
    private static boolean bungeeCmd = true;

    @CommandAlias("bungeecmd")
    @CommandPermission("kauri.command.bungee")
    public void onCommand(Player player, String[] args) {
        if(!bungeeCmd) {
            player.sendMessage(Color.Red + "Bungee command is disabled!");
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Color.Red + "Invalid arguments.");
            return;
        }
        BungeeAPI.sendCommand(String.join(" ", args));
    }
}
