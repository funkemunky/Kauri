package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.utils.Pastebin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Init(commands = true)
public class LogCommand {

    @Command(name = "kauri.logs", description = "View the logs of a user.", display = "logs [player]",
            usage = "/<command> [player]", aliases = {"logs"}, permission = "kauri.logs")
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length == 0) {
            if(cmd.getSender() instanceof Player) {
                cmd.getSender().sendMessage(Color.Green + "Logs: " + getLogsFromUUID(cmd.getPlayer().getUniqueId()));
            } else cmd.getSender().sendMessage(Color.Red + "You cannot view your own logs since you are not a player.");
        } else {
            cmd.getSender().sendMessage(Color.Green + "Logs: " + getLogsFromUUID(Bukkit.getOfflinePlayer(cmd.getArgs()[0]).getUniqueId()));
        }
    }

    private static String getLogsFromUUID(UUID uuid) {
        Kauri.INSTANCE.profiler.start("cmd:logs");
        List<Log> logs = Kauri.INSTANCE.loggerManager.getLogs(uuid);
        Kauri.INSTANCE.profiler.stop("cmd:logs");

        if(logs.size() == 0) return "No Logs";

        StringBuilder body = new StringBuilder();

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/YYYY hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

        OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
        for (Log log : logs) {
            body.append("(").append(format.format(new Date(log.timeStamp))).append("): ").append(pl.getName())
                    .append(" failed ").append(log.checkName).append(" at VL ").append(log.vl)
                    .append(" (tps=").append(MathUtils.round(log.tps, 4)).append(" ping=").append(log.ping)
                    .append(")").append("\n");
        }

        try {
            return Pastebin.makePaste(body.toString(), pl.getName() + "'s Log", Pastebin.Privacy.UNLISTED);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}
