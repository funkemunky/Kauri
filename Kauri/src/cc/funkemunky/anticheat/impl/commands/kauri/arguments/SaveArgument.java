package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SaveArgument extends FunkeArgument {
    public SaveArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        sender.sendMessage(Color.Gray + "Saving all data...");
        Kauri.getInstance().getLoggerManager().saveToDatabase();
        sender.sendMessage(Color.Green + "Completed!");
    }
}
