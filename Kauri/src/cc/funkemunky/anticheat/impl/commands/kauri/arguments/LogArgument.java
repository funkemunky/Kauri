package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogArgument extends FunkeArgument {
    public LogArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        setPlayerOnly(true);
        addAlias("log");
        addAlias("view");
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        Player player = (Player) sender;

        if (args.length >= 2) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if (target == null) {
                sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "The player \"" + args[0] + "\" could not be found in Mojang's databases.");
                return;
            }

            MenuUtils.openLogGUI(player, target);
        }
    }
}
