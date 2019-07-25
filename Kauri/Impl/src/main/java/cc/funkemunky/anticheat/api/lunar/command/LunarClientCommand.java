package cc.funkemunky.anticheat.api.lunar.command;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import cc.funkemunky.anticheat.api.lunar.user.User;
import cc.funkemunky.api.utils.Init;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Init(commands = true)
public class LunarClientCommand {

    @Command(name = "alunarclient", description = "View who's on lunar client.", permission = "atlas.command.lunarclient", aliases = {"alunar", "alc", "lc", "lunar", "lunarclient"})
    public void onCommand(CommandAdapter cmd) {
        CommandSender sender = cmd.getSender();

        if(cmd.getArgs().length > 0) {
            Player target = Bukkit.getPlayer(cmd.getArgs()[0]);

            if (target != null) {
                User targetData = LunarClientAPI.getInstance().getUserManager().getPlayerData(target);
                if (targetData.isLunarClient()) {
                    sender.sendMessage(ChatColor.GREEN + target.getName() + " is on Lunar Client.");
                } else {
                    sender.sendMessage(ChatColor.RED + target.getName() + " is not on Lunar Client.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The player '" + cmd.getArgs()[0] + "' is not online.");
            }
        } else {
            StringBuilder sb = new StringBuilder(ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString()).append("-------").append(ChatColor.AQUA).append(" Lunar Client").append(ChatColor.AQUA + " Users ").append(ChatColor.GRAY).append(ChatColor.STRIKETHROUGH.toString()).append(" -------\n");

            for (Player player : Atlas.getInstance().getServer().getOnlinePlayers()) {
                User data = LunarClientAPI.getInstance().getUserManager().getPlayerData(player);

                if (data != null && data.isLunarClient()) {
                    sb.append(ChatColor.WHITE).append(data.getName()).append("\n");
                }
            }

            sb.append(ChatColor.GRAY).append(ChatColor.STRIKETHROUGH.toString()).append("-------------------------------");
            sender.sendMessage(sb.toString());
        }
    }
}
