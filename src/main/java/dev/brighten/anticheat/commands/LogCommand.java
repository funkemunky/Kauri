package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
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
                if(cmd.getSender() instanceof Player) {
                    cmd.getSender().sendMessage(Color.Green + "Logs: " + getLogsFromUUID(cmd.getPlayer().getUniqueId()));
                } else cmd.getSender().sendMessage(Color.Red + "You cannot view your own logs since you are not a player.");
            } else {
                cmd.getSender().sendMessage(Color.Green + "Logs: " + getLogsFromUUID(Bukkit.getOfflinePlayer(cmd.getArgs()[0]).getUniqueId()));
            }
        });
    }

    @Command(name = "kauri.logs.import", description = "Import deprecated logs.", display = "logs import",
            usage = "/<command>", consoleOnly = true, permission = "kauri.logs.import")
    public void onCommandImport(CommandAdapter cmd) {
        cmd.getSender().sendMessage(Color.Gray + "Importing from old file...");
        Kauri.INSTANCE.loggerManager.convertDeprecatedLogs();
        cmd.getSender().sendMessage(Color.Green + "Completed import. This does not need to be run again.");
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
