package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuArgument extends FunkeArgument {
    public MenuArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        setPlayerOnly(true);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        Player player = (Player) sender;

        if(args.length > 2 && args[1].equals("checks")) {
            try {
                MenuUtils.openCheckEditGUI(player, Integer.parseInt(args[2]));
                return;
            } catch(NumberFormatException e) {

            }
        }
        MenuUtils.openMainGUI(player);
    }
}
