package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.anticheat.impl.config.CheckSettings;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DelayArgument extends FunkeArgument {
    public DelayArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Message(name = "command.delay.setDelay")
    private String message = "&7Set the delay for alerts to &f%ms%ms&7.";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length > 1) {
            try {
                long delay = Long.parseLong(args[1]);

                Kauri.getInstance().getConfig().set("alerts.alertsDelay", CheckSettings.alertsDelay = delay);
                Kauri.getInstance().saveConfig();

                sender.sendMessage(Color.translate(message.replace("%ms%", String.valueOf(delay))));
            } catch (NumberFormatException e) {
                sender.sendMessage(Color.translate(Messages.invalidArgumentsInteger));
            }
        }
    }
}
