package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.logging.Violation;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LogArgument extends FunkeArgument {

    public LogArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("log");
        addAlias("view");
        addTabComplete(2, "gui", "web", "details", "clear");
        addTabComplete(4, "all,clear,2");
        addTabComplete(4, Kauri.getInstance().getCheckManager().getChecks().stream()
                .map(check -> check.getName().replace(" ", "_") + ",clear,2")
                .collect(Collectors.toList())
                .toArray(new String[] {}));
    }

    @Message(name = "command.log.viewWeb")
    private static String viewWeb = "&aView the log here&7: &f%url%";

    @Message(name = "command.log.noLogs")
    private static String noLogs = "&cThis player does not have any logs.";

    @Message(name = "command.log.loggingDisabled")
    private static String loggingDisabled = "&cLogging is currently disabled in the config.";

    @Message(name = "command.log.clear.invalidArgs")
    private static String invalidArgs = "&cInvalid arguments.\n&7Options: &fall&7, &f[check name]";

    @Message(name = "command.log.clear.all")
    private static String clearedAllLogs = "&7Cleared all logs from &c%player%&7.";

    @Message(name = "command.log.clear.specific")
    private static String clearSpecificLogs = "&7Cleared all logs for &f%check% &7from &f%player%&7.";

    @ConfigSetting(path = "command.log", name = "defaultInterface")
    private static String defaultInterface = "web";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if(!Kauri.getInstance().getLoggerManager().enabled) {
            sender.sendMessage(Color.translate(loggingDisabled));
            return;
        }
        if (args.length > 2) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

            if (target == null) {
                sender.sendMessage(Color.translate(Messages.playerDoesntExist));
                return;
            }
            switch (args[1].toLowerCase()) {
                case "gui": {
                    if(sender instanceof Player) {
                        Player player = (Player) sender;

                        MenuUtils.openLogGUI(player, target);
                    } else sender.sendMessage(Color.translate(Messages.playersOnly));
                    break;
                }
                case "web": {
                    runWebLog(sender, target);
                    break;
                }
                case "clear": {
                    if(args.length > 3) {
                        switch(args[3].toLowerCase()) {
                            case "all":
                            case "everything":
                            case "gtfo": {
                                Kauri.getInstance().getLoggerManager().clearLogs(target.getUniqueId());
                                sender.sendMessage(Color.translate(clearedAllLogs.replace("%player%", target.getName())));
                                break;
                            }
                            default: {
                                String arg = args[3].replace("_", " ");
                                if(Kauri.getInstance().getCheckManager().isCheck(arg)) {

                                    Kauri.getInstance().getLoggerManager().clearLogs(target.getUniqueId(), arg);
                                    sender.sendMessage(Color.translate(clearSpecificLogs.replace("%check%", arg).replace("%player%", target.getName())));
                                } else {
                                    sender.sendMessage(Color.translate(invalidArgs));
                                }
                                break;
                            }
                        }
                    } else sender.sendMessage(Color.translate(invalidArgs));
                    break;
                }
                case "details": {
                    if(args.length > 3) {
                        int page = Math.max(1, MathUtils.tryParse(args[3]));
                        runDetailedLogs(target, sender, page);
                    } else {
                        runDetailedLogs(target, sender, 1);
                    }
                    break;
                }
                default: {
                    sender.sendMessage(Color.translate(Messages.invalidArguments));
                    break;
                }
            }
        } else if (args.length > 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if(!defaultInterface.equals("web") && sender instanceof Player) {
                if (target == null) {
                    sender.sendMessage(Color.translate(Messages.playerDoesntExist));
                    return;
                }

                Player player = (Player) sender;

                MenuUtils.openLogGUI(player, target);
            } else {
                runWebLog(sender, target);
            }
        } else {
            sender.sendMessage(Color.translate(Messages.invalidArguments));
        }
    }

    private void runWebLog(CommandSender sender, OfflinePlayer target) {
        val violations = Kauri.getInstance().getLoggerManager().getViolations(target.getUniqueId());

        StringBuilder url = new StringBuilder("https://funkemunky.cc/api/kauri?uuid=" + target.getUniqueId().toString().replaceAll("-", "") + (violations.keySet().size() > 0 ? "&violations=" : ""));

        if (violations.keySet().size() > 0) {
            for (String key : violations.keySet()) {
                if (Kauri.getInstance().getCheckManager().isCheck(key)) {
                    Check check = Kauri.getInstance().getCheckManager().getCheck(key);
                    int vl = violations.get(key), maxVL = check.getMaxVL();
                    boolean developer = check.isDeveloper();

                    String toAppend = key + ":" + vl + ":" + maxVL + ":" + developer + ";";
                    toAppend = toAppend.replaceAll(" ", "%20");

                    url.append(toAppend);

                }
            }

            if (violations.keySet().size() > 0) {
                url.deleteCharAt(url.length() - 1);
            }

            String finalURL = "https://funkemunky.cc/api/kauri/cache/%id%";

            try {
                URL url2Run = new URL(url.toString());
                //%3F
                BufferedReader reader = new BufferedReader(new InputStreamReader(url2Run.openConnection().getInputStream(), Charset.forName("UTF-8")));

                finalURL = finalURL.replace("%id%", readAll(reader));
            } catch (IOException e) {
                e.printStackTrace();
            }

            sender.sendMessage(Color.translate(viewWeb.replace("%url%", finalURL)));
        } else {
            sender.sendMessage(Color.translate(noLogs));
        }
    }

    private void runDetailedLogs(OfflinePlayer target, CommandSender sender, int page) {
        List<Violation> violations = Kauri.getInstance().getLoggerManager().getDetailedViolations(target.getUniqueId());

        violations.sort(Comparator.comparing(Violation::getTimeStamp, Comparator.reverseOrder()));

        if(violations.size() > 0) {
            int pageMin = Math.min((page - 1) * 15, violations.size()), pageMax = Math.min(page * 15, violations.size());

            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
            sender.sendMessage(Color.translate("&7Page: &f" + page + "/" + (int)Math.ceil(violations.size() / 15D)));
            violations.subList(pageMin, pageMax).stream().sorted(Comparator.comparingLong(Violation::getTimeStamp)).map(vio -> Color.translate("&8- &e" + vio.getCheckName() + " &7(&f" + vio.getInfo() + "&7) &fTPS: " + MathUtils.round(vio.getTps(), 2) + " Ping: " + vio.getPing())).forEach(string -> sender.sendMessage(string));
            sender.sendMessage(MiscUtils.line(Color.Dark_Gray));
        } else sender.sendMessage(Color.translate(noLogs));
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
