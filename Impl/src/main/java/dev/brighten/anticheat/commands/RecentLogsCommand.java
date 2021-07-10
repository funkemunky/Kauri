package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;

public class RecentLogsCommand {

    @Command(name = "kauri.recentlogs", aliases = {"recentlogs"}, display = "recentlogs", usage = "/<command>",
            playerOnly = true, permission = "kauri.recentlogs")
    public void onCommand(CommandAdapter cmd) {
        cmd.getPlayer().sendMessage(Color.Green + "Finding recent violators...");
        MenuCommand.getRecentViolatorsMenu(false).showMenu(cmd.getPlayer());
    }
}
