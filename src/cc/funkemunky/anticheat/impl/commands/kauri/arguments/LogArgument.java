package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LogArgument extends FunkeArgument {
    public LogArgument(FunkeCommand command, String name, String display, String description, String... permission) {
        super(command, name, display, description, permission);

        addAlias("viewlog");
        addAlias("logs");
        addAlias("aclogs");


        addTabComplete(2, "clearlog", "verbose", "alerts");
        addTabComplete(3, "all,verbose,2", "all,alerts,2");
        addTabComplete(4, "confirm,clearlog,2");
        List<String> checks = Lists.newArrayList();
        Kauri.getInstance().getCheckManager().getChecks().forEach(check -> checks.add(check.getName().replaceAll(" ", "_")));

        String[] checkArray = new String[checks.size()], checkArray2 = new String[checks.size()];

        for (int i = 0; i < checks.size(); i++) {
            checkArray[i ] = checks.get(i) + ",alerts,2";
            checkArray2[i] = checks.get(i) + ",verbose,2";
        }
        addTabComplete(3, checkArray);
        addTabComplete(3, checkArray2);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if(args.length >= 2) {
           switch(args[1].toLowerCase()) {
               case "clearlog": {
                   if(sender.hasPermission("mercury.log.clearlog")) {
                       if(args.length >= 3) {
                           OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);

                           if(player == null) {
                               sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "No account was found with the name \"" + args[1] + "\"!");
                               return;
                           }

                           if(args.length >= 4) {
                               if(args[3].equalsIgnoreCase("confirm")) {
                                   Kauri.getInstance().getLoggerManager().getLogs().remove(player.getUniqueId());

                                   sender.sendMessage(Color.translate("&aSuccessfully deleted all logs from " + player.getName() + "!"));
                               } else if(args[3].equalsIgnoreCase("all") && sender instanceof ConsoleCommandSender) {
                                   if(args.length > 4 && args[4].equalsIgnoreCase("confirm")) {
                                       Kauri.getInstance().getLoggerManager().getLogCollection().drop();
                                       sender.sendMessage(Color.translate("&aSuccessfully cleared all logs."));
                                   } else {
                                       sender.sendMessage(Color.translate("&7Are you sure you want to delete all of " + player.getName() + "'s logs? Type /&fmercury " + args[0].toLowerCase() + " clearlog " + player.getName() + " confirm &7to continue."));
                                   }
                               } else {
                                   sender.sendMessage(Color.translate("&7Are you sure you want to delete all of " + player.getName() + "'s logs? Type /&fmercury " + args[0].toLowerCase() + " clearlog " + player.getName() + " confirm &7to continue."));
                               }
                           }
                       }
                   } else {
                       sender.sendMessage(Color.Red + "No permission.");
                   }
                   break;
               }
               case "alerts":
               case "verbose": {
                   if(args.length >= 4) {
                       if(args[2].equalsIgnoreCase("all")) {
                           OfflinePlayer player = Bukkit.getOfflinePlayer(args[3]);

                           if(player == null) {
                               sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "No account was found with the name \"" + args[1] + "\"!");
                               return;
                           }

                           int page = 1;

                           if(args.length > 4) {
                               try {
                                   page = Integer.parseInt(args[4]);
                               } catch (Exception e) {
                                   //Empty catch block
                               }
                           }

                           Kauri.getInstance().getLoggerManager().getLogHandler().viewLoggedAlerts(sender, player.getUniqueId(), page);
                       } else {
                           if(Kauri.getInstance().getCheckManager().isCheck(args[2])) {
                               OfflinePlayer player = Bukkit.getOfflinePlayer(args[3]);

                               if(player == null) {
                                   sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "No account was found with the name \"" + args[1] + "\"!");
                                   return;
                               }

                               int page = 1;

                               if(args.length > 4) {
                                   try {
                                       page = Integer.parseInt(args[4]);
                                   } catch (Exception e) {
                                       //Empty catch block
                                   }
                               }

                               Kauri.getInstance().getLoggerManager().getLogHandler().viewLoggedAlerts(sender, player.getUniqueId(), Kauri.getInstance().getCheckManager().getCheck(args[2]).getName(), page);
                           } else {
                               sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "\"" + args[2] + "\" is not a check. Please input an actual check name./");
                           }
                       }
                   }
                   break;
               }
               default: {
                   OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

                   if(player == null) {
                       sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "No account was found with the name \"" + args[1] + "\"!");
                       return;
                   }

                   if(sender instanceof Player) {
                       if(args.length >= 3 && args[2].equalsIgnoreCase("text")) {
                           Kauri.getInstance().getLoggerManager().getLogHandler().viewLoggedVLByText(sender, player.getUniqueId());
                       } else {
                           Kauri.getInstance().getLoggerManager().getLogHandler().viewLoggedVLByGUI((Player) sender, player.getUniqueId());
                       }
                   } else {
                       Kauri.getInstance().getLoggerManager().getLogHandler().viewLoggedVLByText(sender, player.getUniqueId());
                   }
                   break;
               }
           }
            return;
        }
        sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
    }
}
