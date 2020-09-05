package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;

@Init(commands = true)
public class ToggleCommand {

    @Command(name = "kauri.toggle", description = "Toggle a check on or off.", display = "toggle",
            usage = "/<command>", aliases = {"toggleCheck", "tCheck", "kauri.t"}, permission = "kauri.command.toggle")
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
           if(Check.isCheck(cmd.getArgs()[0])) {
               CheckInfo check = Check.getCheckInfo(cmd.getArgs()[0].replace("_", " "));

               String path = "checks." + check.name() + ".enabled";

               boolean toggleState = !Kauri.INSTANCE.getConfig().getBoolean(path);

               cmd.getSender().sendMessage(Color.Gray + "Setting check state to "
                       + (toggleState ? Color.Green : Color.Red) + toggleState + Color.Gray + "...");
               cmd.getSender().sendMessage(Color.Red + "Setting in config...");
               Kauri.INSTANCE.getConfig().set(path, toggleState);
               Kauri.INSTANCE.saveConfig();

               cmd.getSender().sendMessage(Color.Red + "Refreshing data objects with updated information...");
               Kauri.INSTANCE.dataManager.dataMap.values().parallelStream()
                       .forEach(data -> data.checkManager.checks.get(check.name()).enabled = toggleState);
               cmd.getSender().sendMessage(Color.Green + "Completed!");
           } else cmd.getSender().sendMessage(Color.Red + "\"" + cmd.getArgs()[0].replace("_", " ") + "\" is not a check.");
        } else cmd.getSender().sendMessage(Color.Red + "Invalid arguments. Usage: /" + cmd.getLabel());
    }
}
