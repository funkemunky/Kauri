package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Banwave extends FunkeArgument {
    public Banwave(FunkeCommand parent, String name, String display, String description) {
        super(parent, name, display, description);
    }

    @Override
    public void onArgument(CommandSender commandSender, Command command, String[] strings) {
        commandSender.sendMessage(Color.Green + "Forced the ban wave.");
        Kauri.getInstance().getBanwaveManager().runJudgementDay();
    }
}
