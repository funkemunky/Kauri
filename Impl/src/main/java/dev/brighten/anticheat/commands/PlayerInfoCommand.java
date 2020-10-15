package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.menu.PlayerInformationGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Init(commands = true)
public class PlayerInfoCommand {

    @Command(name = "kauri.info", description = "get the information of a player", display = "info [player]",
            aliases = {"info", "pi", "kauri.pi", "kauri.pinfo", "kauri.playerinfo", "pinfo"}, playerOnly = true,
            permission = "kauri.command.info")
    public void onCommand(CommandAdapter cmd) {
        Kauri.INSTANCE.executor.execute(() -> {
            if(cmd.getArgs().length > 0) {
                Player player = Bukkit.getPlayer(cmd.getArgs()[0]);

                if(player != null) {
                    ObjectData targetData = Kauri.INSTANCE.dataManager.getData(player);

                    if(targetData != null) {
                        PlayerInformationGUI info = new PlayerInformationGUI(targetData);

                        info.showMenu(cmd.getPlayer());
                        cmd.getPlayer().sendMessage(Color.Green + "Opened menu.");
                    } else cmd.getSender()
                            .sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                                    "&cThere was an error trying to find your data."));
                } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                        .msg("player-not-online", "&cThe player provided is not online!"));
            } else cmd.getSender().sendMessage(Color.Red + "Invalid arguments.");
        });
    }
}
