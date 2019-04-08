package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SaveArgument extends FunkeArgument {
    public SaveArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Message(name = "command.save.started")
    private String started = "&7Saving all data...";

    @Message(name = "command.save.completed")
    private String completed = "&aCompleted!";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        sender.sendMessage(Color.translate(started));
        Kauri.getInstance().getLoggerManager().saveToDatabase();
        sender.sendMessage(Color.translate(completed));
    }
}
