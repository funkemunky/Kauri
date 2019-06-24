package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
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


    @Message(name = "command.bypassing.isBypassing")
    private String isBypassing = "&7The player &f%player% &7is now bypassing Kauri's checks.";

    @Message(name = "command.bypassing.notBypassing")
    private String notBypassing = "&7The player &f%player% &7is no longer bypassing Kauri's checks.";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length > 1) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

            if (player != null) {
                boolean bypassing = !Kauri.getInstance().getCheckManager().isBypassing(player.getUniqueId());

                if (args.length > 2) bypassing = Boolean.parseBoolean(args[2]);

                Kauri.getInstance().getCheckManager().setBypassing(player.getUniqueId(), bypassing);

                sender.sendMessage(Color.translate((bypassing ? isBypassing : notBypassing).replace("%player%", player.getName())));
            } else {
                sender.sendMessage(Color.translate(Messages.playerDoesntExist));
            }
        } else {
            sender.sendMessage(Color.translate(Messages.invalidArguments));
        }
    }
}
