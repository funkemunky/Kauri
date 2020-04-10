package dev.brighten.anticheat.utils.lunar.command;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.utils.lunar.LunarClientAPI;
import dev.brighten.anticheat.utils.lunar.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LunarClientCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target != null) {
                User targetData = LunarClientAPI.getInstance().getUserManager().getPlayerData(target);
                if (targetData.isLunarClient()) {
                    sender.sendMessage(ChatColor.GREEN + target.getName() + " is on Lunar Client.");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + target.getName() + " is not on Lunar Client.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The player '" + args[0] + "' is not online.");
                return false;
            }
        } else {
            StringBuilder sb = new StringBuilder(ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString()).append("-------").append(ChatColor.AQUA).append(" Lunar Client").append(ChatColor.AQUA + " Users ").append(ChatColor.GRAY).append(ChatColor.STRIKETHROUGH.toString()).append(" -------\n");

            for (Player player : Kauri.INSTANCE.getServer().getOnlinePlayers()) {
                User data = LunarClientAPI.getInstance().getUserManager().getPlayerData(player);

                if (data != null && data.isLunarClient()) {
                    sb.append(ChatColor.WHITE).append(data.getName()).append("\n");
                }
            }

            sb.append(ChatColor.GRAY).append(ChatColor.STRIKETHROUGH.toString()).append("-------------------------------");
            sender.sendMessage(sb.toString());
            return true;
        }
    }
}
