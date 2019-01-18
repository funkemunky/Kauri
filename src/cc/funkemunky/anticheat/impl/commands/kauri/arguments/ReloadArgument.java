package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadArgument extends FunkeArgument {
    public ReloadArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Override
    public void onArgument(CommandSender commandSender, Command command, String[] strings) {
        commandSender.sendMessage(Color.Red + "Reloading configuration...");
        Kauri.getInstance().reloadConfig();
        Kauri.getInstance().getCheckManager().reloadChecks();
        commandSender.sendMessage(Color.Green + "Completed!");
    }
}
