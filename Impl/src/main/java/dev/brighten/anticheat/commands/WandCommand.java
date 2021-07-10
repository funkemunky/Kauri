package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.XMaterial;
import dev.brighten.anticheat.listeners.generalChecks.BukkitListener;

import java.util.Arrays;

public class WandCommand {

    @Command(name = "kauri.wand", description = "view boundingBox debugs.", display = "wand", playerOnly = true, permission = "kauri.command.wand")
    public void onCommand(CommandAdapter cmd) {
        if(Arrays.stream(cmd.getPlayer().getInventory().getContents())
                .anyMatch(item -> item == null || item.getType().equals(XMaterial.AIR.parseMaterial()))) {
            cmd.getPlayer().getInventory().addItem(BukkitListener.MAGIC_WAND);
            cmd.getPlayer().updateInventory();
        } else {
            cmd.getPlayer().getWorld().dropItemNaturally(cmd.getPlayer().getLocation(), BukkitListener.MAGIC_WAND);
            cmd.getPlayer().sendMessage(Color.Red + Color.Italics + "Your inventory was full. Item dropped onto ground.");
        }
        cmd.getPlayer().sendMessage(Color.Green + "Added a magic wand to your inventory. Use it wisely.");
    }
}
