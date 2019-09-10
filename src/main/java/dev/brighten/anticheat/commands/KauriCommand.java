package dev.brighten.anticheat.commands;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Init;

@Init(commands = true)
public class KauriCommand {

    @Command(name = "kauri", description = "The Kauri main command.", display = "Kauri", aliases = {"anticheat"})
    public void onCommand(CommandAdapter cmd) {
        Atlas.getInstance().getCommandManager().runHelpMessage(cmd, cmd.getSender(), Atlas.getInstance().getCommandManager().getDefaultScheme());
    }
}
