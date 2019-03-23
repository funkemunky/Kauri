package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BypassingArgument extends FunkeArgument {
    public BypassingArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addTabComplete(3, "true");
        addTabComplete(3, "false");
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length > 0) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

            if (player != null) {
                boolean bypassing = Kauri.getInstance().getCheckManager().isBypassing(player.getUniqueId());

                if (args.length > 1) bypassing = Boolean.parseBoolean(args[1]);

                Kauri.getInstance().getCheckManager().setBypassing(player.getUniqueId(), bypassing);
            }
        } else {
            sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
        }
    }
}
