package cc.funkemunky.anticheat.impl.commands.kauri.arguments;


import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecentArgument extends FunkeArgument {

    public RecentArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        setPlayerOnly(true);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        Player player = (Player) sender;

        Kauri.getInstance().getLoggerManager().getLogHandler().getRecentlyFlagged(player);
    }
}
