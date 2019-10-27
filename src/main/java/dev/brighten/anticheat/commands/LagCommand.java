package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.profiling.ResultsType;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Init(commands = true)
public class LagCommand {

    @Command(name = "kauri.lag", description = "view important lag information", display = "lag",
            aliases = {"lag", "klag"}, permission = "kauri.lag")
    public void onCommand(CommandAdapter cmd) {
        cmd.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
        cmd.getSender().sendMessage(Color.Gold + Color.Bold + "Server Lag Information");
        cmd.getSender().sendMessage("");
        cmd.getSender().sendMessage(Color.translate("&eTPS&7: &f" +
                MathUtils.round(Kauri.INSTANCE.tps, 2)));
        double totalMem =  Runtime.getRuntime().totalMemory();
        double freeMem = Runtime.getRuntime().freeMemory();
        cmd.getSender().sendMessage(Color.translate("&eMemory Used&7: &f" +
                MathUtils.round(totalMem - freeMem * 1e-6, 4) + "MB"));
        cmd.getSender().sendMessage(Color.translate("&eKauri CPU Usage:" +
                MathUtils.round(50D / Kauri.INSTANCE.profiler.results(ResultsType.TICK).values()
                        .stream()
                        .mapToDouble(val -> val.two)
                        .sum())));
        cmd.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
    }

    @Command(name = "kauri.lag.player", description = "view player lag", display = "lag player [player]",
            aliases = {"lag.player", "klag.player"}, permission = "kauri.lag")
    public void onLagPlayer(CommandAdapter cmd) {
        Player target;
        if(cmd.getArgs().length == 0) {
            if(cmd.getSender() instanceof Player) {
                target = cmd.getPlayer();
            } else {
                cmd.getSender().sendMessage(Color.Red + "You must provide a player to check.");
                return;
            }
        } else {
            target = Bukkit.getPlayer(cmd.getArgs()[0]);

            if(target == null) {
                cmd.getSender().sendMessage(Color.Red + "The player provided is not online!");
                return;
            }
        }

        ObjectData data = Kauri.INSTANCE.dataManager.getData(target);

        if(data != null) {
            cmd.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
            cmd.getSender().sendMessage(Color.Gold + Color.Bold + target.getName() + "'s Lag Information");
            cmd.getSender().sendMessage("");
            cmd.getSender().sendMessage(Color.translate("&ePing&7: &f"
                    + data.lagInfo.ping + "&7/&f" + data.lagInfo.transPing));
            cmd.getSender().sendMessage(Color.translate("&eLast Skip&7: &f" + data.lagInfo.lastPacketDrop.getPassed()));
            cmd.getSender().sendMessage(Color.translate("&eLagging&7:&f" + data.lagInfo.lagging));
            cmd.getSender().sendMessage(MiscUtils.line(Color.Dark_Gray));
        } else cmd.getSender().sendMessage(Color.Red +
                "There was an error trying to find the data of the target provided.");
    }
}
