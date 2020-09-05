package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;

@Init(commands = true)
public class DelayCommand {


    @Command(name = "kauri.delay", description = "change the delay between alerts.", display = "delay [ms]",
            permission = "kauri.command.delay", aliases = {"delay"}, usage = "/<command> <ms>")
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            try {
                long delay = Long.parseLong(cmd.getArgs()[0]);
                cmd.getSender().sendMessage(Color.Gray + "Setting delay to "
                        + Color.White + delay + "ms" + Color.Gray + "...");

                Config.alertsDelay = delay;
                Kauri.INSTANCE.getConfig().set("alerts.delay", delay);
                Kauri.INSTANCE.saveConfig();
                cmd.getSender().sendMessage(Color.Green + "Delay set!");
            } catch(NumberFormatException e) {
                cmd.getSender().sendMessage(Color.Red + "The argument \"" + cmd.getArgs()[0]
                        + "\" provided is not a long.");
            }
        } else cmd.getSender()
                .sendMessage(Color.Red + "Invalid arguments. Check the help page for more information.");
    }
}
