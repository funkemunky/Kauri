package dev.brighten.anticheat.commands;

import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.co.aikar.commands.BaseCommand;
import cc.funkemunky.api.utils.co.aikar.commands.annotation.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.anticheat.menu.LogsGUI;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.menu.preset.ConfirmationMenu;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Init(priority = Priority.LOW)
@CommandAlias("kauri|anticheat")
@CommandPermission("kauri.command")
public class LogCommand extends BaseCommand {

    @Subcommand("logs")
    @CommandPermission("kauri.command.logs")
    @Syntax("[player]")
    @CommandCompletion("@players")
    @Description("View the logs of a user")
    public void onCommand(CommandSender sender, String[] args) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(args.length == 0) {
                if(sender instanceof Player) {
                    Player player = (Player) sender;
                    LogsGUI gui = new LogsGUI(player.getName(), player.getUniqueId());
                    RunUtils.task(() -> {
                        gui.showMenu(player);
                        sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                .msg("opened-menu", "&aOpened menu."));
                        sender.sendMessage(Color.Green + "Opened menu.");
                    }, Kauri.INSTANCE);
                } else sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("no-console-logs",
                                "&cYou cannot view your own logs since you are not a player."));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

                if(player == null) {
                    sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("offline-player-not-found", "&cSomehow, out of hundreds of millions of"
                                    + "Minecraft accounts, you found one that doesn't exist."));
                    return;
                }

                if(sender instanceof Player) {
                    LogsGUI gui = new LogsGUI(player.getName(), player.getUniqueId());
                    RunUtils.task(() -> {
                        gui.showMenu((Player) sender);
                        sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                .msg("opened-menu", "&aOpened menu."));
                    }, Kauri.INSTANCE);
                } else sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("logs-pastebin",
                                "&aLogs: %pastebin%").replace("%pastebin%",
                                getLogsFromUUID(player.getUniqueId())));
            }
        });
    }

    @Subcommand("logs web")
    @CommandPermission("kauri.command.logs")
    @Syntax("[player]")
    @CommandCompletion("@players")
    @Description("View the logs of a user")
    public void onLogsWeb(CommandSender sender, String[] args) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(args.length == 0) {
                if(sender instanceof Player) {
                    Player player = (Player) sender;

                    runWebLog(sender, player);

                } else sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("no-console-logs",
                                "&cYou cannot view your own logs since you are not a player."));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

                if(player == null) {
                    sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("offline-player-not-found", "&cSomehow, out of hundreds of millions of"
                                    + "Minecraft accounts, you found one that doesn't exist."));
                    return;
                }

                runWebLog(sender, player);
            }
        });
    }


    @Subcommand("logs clear")
    @CommandPermission("kauri.command.logs.clear")
    @Syntax("[playerName]")
    @CommandCompletion("@players")
    @Description("Clear logs of a player")
    public void onLogsClear(CommandSender sender, String[] args) {
        if(args.length > 0) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

            if(player == null) {
                sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("offline-player-not-found", "&cSomehow, out of hundreds of millions of"
                                + "Minecraft accounts, you found one that doesn't exist."));
                return;
            }

            if(sender instanceof Player) {
                ConfirmationMenu menu = new ConfirmationMenu(
                        "Clear " + player.getName() + "'s logs?",
                        (pl, confirmed) -> {
                            if(confirmed) {
                                Kauri.INSTANCE.executor.execute(() -> {
                                    sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                            .msg("clearing-logs", "&7Clearing logs from %player%...")
                                            .replace("%player%", player.getName()));
                                    Kauri.INSTANCE.loggerManager.clearLogs(player.getUniqueId());
                                    sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                            .msg("clear-logs-success", "&aLogs cleared!"));
                                });
                            }
                        });
                menu.showMenu((Player)sender);
            } else {
                Kauri.INSTANCE.executor.execute(() -> {
                    sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("clearing-logs", "&7Clearing logs from %player%...")
                            .replace("%player%", player.getName()));
                    Kauri.INSTANCE.loggerManager.clearLogs(player.getUniqueId());
                    sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("clear-logs-success", "&aLogs cleared!"));
                });
            }
        } else sender.sendMessage(Color.Red + "You must provide the name of a player.");
    }

    private void runWebLog(CommandSender sender, OfflinePlayer target) {
        val logs = Kauri.INSTANCE.loggerManager.getLogs(target.getUniqueId());
        Map<String, Integer> violations = new HashMap<>();
        for (Log log : logs) {
            violations.compute(log.checkName, (name, count) -> {
               if(count == null) {
                   return 1;
               }

               return count + 1;
            });
        }


        StringBuilder url = new StringBuilder("https://funkemunky.cc/api/kauri?uuid=" + target.getUniqueId().toString().replaceAll("-", "") + (violations.keySet().size() > 0 ? "&violations=" : ""));

        if (violations.keySet().size() > 0) {
            for (String key : violations.keySet()) {
                if (Check.isCheck(key)) {
                    CheckInfo check = Check.getCheckInfo(key);
                    int vl = violations.get(key), maxVL = check.punishVL();
                    boolean developer = !check.devStage().isRelease();

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
                BufferedReader reader = new BufferedReader(new InputStreamReader(url2Run
                        .openConnection().getInputStream(), Charset.forName("UTF-8")));

                finalURL = finalURL.replace("%id%", readAll(reader));
            } catch (IOException e) {
                e.printStackTrace();
            }

            sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("logs.viewWeb", "&aView the log here&7: &f%url%")
                    .replace("%url%", finalURL));
        } else {
            sender.sendMessage(Color.translate("&cPlayer has no logs."));
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String getLogsFromUUID(UUID uuid) {
        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(uuid, null, 0, 2000, 0, System.currentTimeMillis());
        List<Punishment> punishments = Kauri.INSTANCE.loggerManager.getPunishments(uuid);

        if(logs.size() == 0) return "No Logs";

        String body;

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);

        SortedMap<Long, String> eventsByStamp = new TreeMap<>(Comparator.comparing(key -> key, Comparator.naturalOrder()));

        logs.forEach(log -> {
            String built = "(" + format.format(new Date(log.timeStamp)) + "): " + pl.getName() + " failed "
                    + log.checkName + " at VL: [" + MathUtils.round(log.vl, 2)
                    + "] (tps=" + MathUtils.round(log.tps, 4) + " ping=" + log.ping + " info=[" + log.info + "])";
            eventsByStamp.put(log.timeStamp, built);
        });
        punishments.forEach(punishment -> {
            String built = "Punishment applied @ (" + format.format(new Date(punishment.timeStamp)) + ") from check "
                    + punishment.checkName;
            eventsByStamp.put(punishment.timeStamp, built);
        });

        body = eventsByStamp.keySet().stream().map(key -> eventsByStamp.get(key) + "\n").collect(Collectors.joining());

        try {
            return Pastebin.makePaste(body, pl.getName() + "'s Log", Pastebin.Privacy.UNLISTED);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}
