package dev.brighten.anticheat.commands;

import cc.funkemunky.api.bungee.BungeeAPI;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;

@Init(commands = true,priority = Priority.LOW)
public class BungeeCommand {

    @ConfigSetting(path = "general", name = "bungeeCmd")
    private static boolean bungeeCmd = true;

    @Command(name = "bungeecmd", display = "bungee [args]", description = "send command to bungee",
            permission = "kauri.command.bungee")
    public void onCommand(CommandAdapter cmd) {
        if(!bungeeCmd) {
            cmd.getPlayer().sendMessage(Color.Red + "Bungee command is disabled!");
            return;
        }
        if(cmd.getArgs().length == 0) {
            cmd.getPlayer().sendMessage(Color.Red + "Invalid arguments.");
            return;
        }
        BungeeAPI.sendCommand(String.join(" ", cmd.getArgs()));
    }
}
