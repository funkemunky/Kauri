package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.profiling.ResultsType;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import dev.brighten.anticheat.utils.StringUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicLong;

@Init(commands = true)
public class LagCommand {

    private static String getMsg(String name, String def) {
        return Kauri.INSTANCE.msgHandler.getLanguage().msg("command.lag." + name, def);
    }

    @Command(name = "kauri.lag", description = "view important lag information", display = "lag",
            aliases = {"lag", "klag"}, permission = "kauri.command.lag")
    public void onCommand(CommandAdapter cmd) {
        StringUtils.Messages.LINE.send(cmd.getSender());
        MiscUtils.sendMessage(cmd.getSender(), getMsg("main.title",
                Color.Gold + Color.Bold + "Server Lag Information"));
        cmd.getSender().sendMessage("");
        MiscUtils.sendMessage(cmd.getSender(), getMsg("main.tps", "&eTPS&8: &f%.2f\\%"),
                Kauri.INSTANCE.getTps());
        AtomicLong chunkCount = new AtomicLong(0);
        Bukkit.getWorlds().forEach(world -> chunkCount.addAndGet(world.getLoadedChunks().length));
        MiscUtils.sendMessage(cmd.getSender(), getMsg("main.chunks", "&eChunks&8: &f%s"), chunkCount.get());
        MiscUtils.sendMessage(cmd.getSender(), getMsg("main.memory",
                "&eMemory &7(&f&oFree&7&o/&f&oTotal&7&o/&f&oAllocated&7)&8: &f%.2fGB&7/&f%.2fGB&7/&f%.2fGB"),
                Runtime.getRuntime().freeMemory() / 1E9,
                Runtime.getRuntime().totalMemory() / 1E9, Runtime.getRuntime().maxMemory() / 1E9);
        val results = Kauri.INSTANCE.profiler.results(ResultsType.TOTAL);
        MiscUtils.sendMessage(cmd.getSender(), getMsg("main.cpu-usage", "&eKauri CPU Usage&8: &f%.5f\\%"),
                results.keySet().stream()
                .filter(key -> !key.contains("check:"))
                .mapToDouble(key -> results.get(key).two / 1000000D)
                .filter(val -> !Double.isNaN(val) && !Double.isInfinite(val))
                .sum() / 50D * 100);
        StringUtils.Messages.LINE.send(cmd.getSender());
    }

    @Command(name = "kauri.lag.gc", description = "run a garbage collector.", display = "lag gc",
            aliases = {"lag.gc", "klag.gc"}, permission = "kauri.command.lag.gc")
    public void onLagGc(CommandAdapter cmd) {
        cmd.getSender().sendMessage(getMsg("start-gc", "&7Starting garbage collector..."));

        long stamp = System.nanoTime();
        double time;
        Runtime.getRuntime().gc();
        time = (System.nanoTime() - stamp) / 1E6D;

        StringUtils.Messages.GC_COMPLETE.send(cmd.getSender(), time);
    }

    @Command(name = "kauri.lag.player", description = "view player lag", display = "lag player [player]",
            aliases = {"lag.player", "klag.player"}, permission = "kauri.command.lag.player")
    public void onLagPlayer(CommandAdapter cmd) {
        Player target;
        if(cmd.getArgs().length == 0) {
            if(cmd.getSender() instanceof Player) {
                target = cmd.getPlayer();
            } else {
                StringUtils.Messages.PROVIDE_PLAYER.send(cmd.getSender());
                return;
            }
        } else {
            target = Bukkit.getPlayer(cmd.getArgs()[0]);

            if(target == null) {
                StringUtils.Messages.PLAYER_ONLINE_NO.send(cmd.getSender());
                return;
            }
        }

        ObjectData data = Kauri.INSTANCE.dataManager.getData(target);

        if(data != null) {
            StringUtils.Messages.LINE.send(cmd.getSender());
            StringUtils.sendMessage(cmd.getSender(), Color.Gold + Color.Bold + target.getName() + "'s Lag Information");
            StringUtils.sendMessage(cmd.getSender(), "");
            StringUtils.sendMessage(cmd.getSender(), "&ePing&7: &f"
                    + data.lagInfo.ping + "ms&7/&f" + data.lagInfo.transPing + " tick");
            StringUtils.sendMessage(cmd.getSender(), "&eLast Skip&7: &f" + data.lagInfo.lastPacketDrop.getPassed());
            StringUtils.sendMessage(cmd.getSender(), "&eLagging&7: &f" + data.lagInfo.lagging);
            StringUtils.Messages.LINE.send(cmd.getSender());
        } else StringUtils.Messages.DATA_ERROR.send(cmd.getSender());
    }
}
