package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecentViolationsArgument extends FunkeArgument {

    @Message(name = "command.recentViolations.opened")
    private String opened = "&aYou are now viewing the recent violators.";

    public RecentViolationsArgument(FunkeCommand parent, String name, String display, String description, String... permissions) {
        super(parent, name, display, description, permissions);

        setPlayerOnly(true);
        addAlias("recentViolations");
        addAlias("violators");
        addAlias("recentvio");
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        Player player = (Player) sender;

        MenuUtils.openRecentViolators(player, 1);

        player.sendMessage(Color.translate(opened));
    }
}
