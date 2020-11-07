package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.profiling.ResultsType;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicLong;

@Init(commands = true)
public class LagCommand {

    @Command(name = "kauri.lag", description = "view important lag information", display = "lag",
            aliases = {"lag", "klag"}, permission = "kauri.command.lag")
    public void onCommand(CommandAdapter cmd) {
        StringUtils.Messages.LINE.send(cmd.getSender());
        cmd.getSender().sendMessage(Color.Gold + Color.Bold + "Server Lag Information");
        cmd.getSender().sendMessage("");
        cmd.getSender().sendMessage(Color.translate("&eTPS&8: &f" +
                MathUtils.round(Kauri.INSTANCE.getTps(), 2)));
        AtomicLong chunkCount = new AtomicLong(0);
        Bukkit.getWorlds().forEach(world -> chunkCount.addAndGet(world.getLoadedChunks().length));
        cmd.getSender().sendMessage(Color.translate("&eChunks&8: &f" + chunkCount.get()));
        double totalMem =  MathUtils.round(Runtime.getRuntime().totalMemory() / 1E9, 2);
        double freeMem = MathUtils.round(Runtime.getRuntime().freeMemory() / 1E9, 2);
        double allocated = MathUtils.round(Runtime.getRuntime().maxMemory() / 1E9, 2);
        cmd.getSender().sendMessage(Color.translate("&eMemory (GB)&8: &f"
                + freeMem + "&7/&f" + totalMem + "&7/&f" + allocated));
        cmd.getSender().sendMessage(Color.translate("&eKauri CPU Usage&8: &f" +
                MathUtils.round(Kauri.INSTANCE.profiler.results(ResultsType.TICK).values()
                        .stream()
                        .mapToDouble(val -> val.two / 1000000D)
                        .filter(val -> !Double.isNaN(val) && !Double.isInfinite(val))
                        .sum() / 50D * 100, 1)) + "%");
        StringUtils.Messages.LINE.send(cmd.getSender());
    }

    @Command(name = "kauri.lag.gc", description = "run a garbage collector.", display = "lag gc",
            aliases = {"lag.gc", "klag.gc"}, permission = "kauri.command.lag.gc")
    public void onLagGc(CommandAdapter cmd) {
        cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("start-gc", "&7Starting garbage collector..."));

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
