package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.anticheat.menu.LogsGUI;
import dev.brighten.anticheat.utils.Pastebin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Init(commands = true)
public class LogCommand {

    @Command(name = "kauri.logs", description = "View the logs of a user.", display = "logs [player]",
            usage = "/<command> [player]", aliases = {"logs"}, permission = "kauri.logs")
    public void onCommand(CommandAdapter cmd) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(cmd.getArgs().length == 0) {
                if(cmd.getPlayer() != null) {
                    LogsGUI gui = new LogsGUI(cmd.getPlayer());
                    gui.showMenu(cmd.getPlayer());
                    cmd.getSender().sendMessage(Color.Green + "Opened menu.");
                } else cmd.getSender().sendMessage(Color.Red + "You cannot view your own logs since you are not a player.");
            } else {
                OfflinePlayer player = Bukkit.getOfflinePlayer(cmd.getArgs()[0]);

                if(player == null) {
                    cmd.getSender().sendMessage(Color.Red + "Somehow, out of hundreds of millions of Minecraft"
                            + " accounts, you found one that doesn't exist.");
                    return;
                }

                if(cmd.getPlayer() != null) {
                    LogsGUI gui = new LogsGUI(player);
                    gui.showMenu(cmd.getPlayer());
                    cmd.getSender().sendMessage(Color.Green + "Opened menu.");
                } else cmd.getSender().sendMessage(Color.Green + "Logs: " + getLogsFromUUID(Bukkit.getOfflinePlayer(cmd.getArgs()[0]).getUniqueId()));
            }
        });
    }

    @Command(name = "kauri.logs.clear", display = "logs clear [player]", description = "Clear logs of a player",
            usage = "/<command> [playerName]",  permission = "kauri.logs.clear")
    public void onLogsClear(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(cmd.getArgs()[0]);

            if(player == null) {
                cmd.getSender().sendMessage(Color.Red + "Somehow, out of hundreds of millions of Minecraft"
                        + " accounts, you found one that doesn't exist.");
                return;
            }

            cmd.getSender().sendMessage(Color.Gray + "Clearing logs from " + player.getName() + "...");
            Kauri.INSTANCE.loggerManager.clearLogs(player.getUniqueId());
            cmd.getSender().sendMessage(Color.Green + "Logs cleared!");
        } else cmd.getSender().sendMessage(Color.Red + "You must provide the name of a player.");
    }

    public static String getLogsFromUUID(UUID uuid) {
        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(uuid);
        List<Punishment> punishments = Kauri.INSTANCE.loggerManager.getPunishments(uuid);

        if(logs.size() == 0) return "No Logs";

        StringBuilder body = new StringBuilder();

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);

        SortedMap<Long, String> eventsByStamp = new TreeMap<>(Comparator.comparing(key -> key, Comparator.naturalOrder()));

        for (Log log : logs) {
            String built = "(" + format.format(new Date(log.timeStamp)) + "): " + pl.getName() + " failed "
                    + log.checkName + " at VL: [" + MathUtils.round(log.vl, 2)
                    + "] (tps=" + MathUtils.round(log.tps, 4) + " ping=" + log.ping + " info=[" + log.info + "])";
            eventsByStamp.put(log.timeStamp, built);
        }

        for (Punishment punishment : punishments) {
            String built = "Punishment applied @ (" + format.format(new Date(punishment.timeStamp)) + ") from check "
                    + punishment.checkName;
            eventsByStamp.put(punishment.timeStamp, built);
        }

        for (Long key : eventsByStamp.keySet()) {
            body.append(eventsByStamp.get(key)).append("\n");
        }

        try {
            return Pastebin.makePaste(body.toString(), pl.getName() + "'s Log", Pastebin.Privacy.UNLISTED);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}
