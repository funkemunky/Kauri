package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.anticheat.menu.LogsGUI;
import dev.brighten.anticheat.utils.Pastebin;
import dev.brighten.anticheat.utils.menu.preset.ConfirmationMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Init(commands = true)
public class LogCommand {

    @Command(name = "kauri.logs", description = "View the logs of a user.", display = "logs [player]",
            usage = "/<command> [player]", aliases = {"logs"}, permission = "kauri.command.logs")
    public void onCommand(CommandAdapter cmd) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(cmd.getArgs().length == 0) {
                if(cmd.getPlayer() != null) {
                    LogsGUI gui = new LogsGUI(cmd.getPlayer());
                    RunUtils.task(() -> {
                        gui.showMenu(cmd.getPlayer());
                        cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                .msg("opened-menu", "&aOpened menu."));
                        cmd.getSender().sendMessage(Color.Green + "Opened menu.");
                    }, Kauri.INSTANCE);
                } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("no-console-logs",
                                "&cYou cannot view your own logs since you are not a player."));
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(cmd.getArgs()[0]);

                if(player == null) {
                    cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("offline-player-not-found", "&cSomehow, out of hundreds of millions of"
                                    + "Minecraft accounts, you found one that doesn't exist."));
                    return;
                }

                if(cmd.getPlayer() != null) {
                    LogsGUI gui = new LogsGUI(player);
                    RunUtils.task(() -> {
                        gui.showMenu(cmd.getPlayer());
                        cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                .msg("opened-menu", "&aOpened menu."));
                    }, Kauri.INSTANCE);
                } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("logs-pastebin",
                                "&aLogs: %pastebin%".replace("%pastebin%",
                                        getLogsFromUUID(player.getUniqueId()))));
            }
        });
    }

    @Command(name = "kauri.logs.clear", display = "logs clear [player]", description = "Clear logs of a player",
            usage = "/<command> [playerName]",  permission = "kauri.command.logs.clear")
    public void onLogsClear(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(cmd.getArgs()[0]);

            if(player == null) {
                cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("offline-player-not-found", "&cSomehow, out of hundreds of millions of"
                                + "Minecraft accounts, you found one that doesn't exist."));
                return;
            }

            if(cmd.getPlayer() != null) {
                ConfirmationMenu menu = new ConfirmationMenu(
                        "Clear " + player.getName() + "'s logs?",
                        (pl, confirmed) -> {
                            if(confirmed) {
                                Kauri.INSTANCE.executor.execute(() -> {
                                    cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                            .msg("clearing-logs", "&7Clearing logs from %player%...")
                                            .replace("%player%", player.getName()));
                                    Kauri.INSTANCE.loggerManager.clearLogs(player.getUniqueId());
                                    cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                                            .msg("clear-logs-success", "&aLogs cleared!"));
                                });
                            }
                        });
                menu.showMenu(cmd.getPlayer());
            } else {
                Kauri.INSTANCE.executor.execute(() -> {
                    cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("clearing-logs", "&7Clearing logs from %player%...")
                            .replace("%player%", player.getName()));
                    Kauri.INSTANCE.loggerManager.clearLogs(player.getUniqueId());
                    cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("clear-logs-success", "&aLogs cleared!"));
                });
            }
        } else cmd.getSender().sendMessage(Color.Red + "You must provide the name of a player.");
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
