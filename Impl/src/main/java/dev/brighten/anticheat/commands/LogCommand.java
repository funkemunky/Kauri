package dev.brighten.anticheat.commands;

import cc.funkemunky.api.utils.*;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.anticheat.menu.LogsGUI;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.menu.preset.ConfirmationMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
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
                    LogsGUI gui = new LogsGUI(player);
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
                    LogsGUI gui = new LogsGUI(player);
                    RunUtils.task(() -> {
                        gui.showMenu((Player) sender);
                        sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                .msg("opened-menu", "&aOpened menu."));
                    }, Kauri.INSTANCE);
                } else sender.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("logs-pastebin",
                                "&aLogs: %pastebin%".replace("%pastebin%",
                                        getLogsFromUUID(player.getUniqueId()))));
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

    public static String getLogsFromUUID(UUID uuid) {
        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(uuid);
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
