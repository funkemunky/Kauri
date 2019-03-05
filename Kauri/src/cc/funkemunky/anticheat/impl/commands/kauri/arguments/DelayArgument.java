package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.CheckSettings;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DelayArgument extends FunkeArgument {
    public DelayArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if(args.length > 0) {
            try {
                long delay = Long.parseLong(args[0]);

                Kauri.getInstance().getConfig().set("alerts.alertsDelay", CheckSettings.alertsDelay = delay);
                Kauri.getInstance().saveConfig();

                sender.sendMessage(getParent().getCommandMessages().getSuccessColor() + "Set the alerts delay to " + getParent().getCommandMessages().getValueColor() + delay + "ms" + getParent().getCommandMessages().getSuccessColor() + ".");
            } catch(NumberFormatException e) {
                sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "The inputted argument must be in the form of a number.");
            }
        }
    }
}
