package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;

@Init(commands = true)
public class ReloadCommand {

    @Command(name = "kauri.reload", description = "reload the plugin.", display = "reload", permission = "kauri.command.reload")
    public void onCommand(CommandAdapter cmd) {
        cmd.getSender().sendMessage(Color.Red + "Reloading Kauri...");
        Kauri.INSTANCE.reloadConfig();
        cmd.getSender().sendMessage(Color.Gray + "- Reloaded config.");
        Kauri.INSTANCE.unload();
        cmd.getSender().sendMessage(Color.Gray + "- Unloaded Kauri.");
        Kauri.INSTANCE.load();
        cmd.getSender().sendMessage(Color.Gray + "- Loaded Kauri.");
        cmd.getSender().sendMessage(Color.Green + "Completed!");
    }
}
