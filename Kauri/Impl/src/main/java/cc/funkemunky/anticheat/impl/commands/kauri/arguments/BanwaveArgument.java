package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BanwaveArgument extends FunkeArgument {

    @Message(name = "command.banwave.forced")
    private static String forcedBW = "&aForced the ban wave.";

    public BanwaveArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Override
    public void onArgument(CommandSender commandSender, Command command, String[] strings) {
        commandSender.sendMessage(Color.translate(forcedBW));
        Kauri.getInstance().getBanwaveManager().runJudgementDay();
    }
}
