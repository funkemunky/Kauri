package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoxWandArgument extends FunkeArgument {
    public BoxWandArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        setPlayerOnly(true);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] strings) {
        Player player = (Player) sender;

        player.getInventory().addItem(MiscUtils.createItem(Material.BLAZE_ROD, 1, Color.Gold + "Magic Box Wand"));
        sender.sendMessage(Color.Green + "Gave you the magic box wand. Use it wisely.");
    }
}
