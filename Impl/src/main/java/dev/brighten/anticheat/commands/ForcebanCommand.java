package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ForcebanCommand {

    @Command(name = "kauri.forceban", display = "ban [player]", description = "Force bans a player.",
            permission = "kauri.command.forceban")
    public void onForceBan(CommandAdapter cmd) {
        if (cmd.getArgs().length > 0) {
            Player target;

            if((target = Bukkit.getPlayer(cmd.getArgs()[0])) != null) {
                ObjectData data = Kauri.INSTANCE.dataManager.getData(target);

                data.checkManager.checks.values().stream().filter(check -> !check.isDeveloper() && check.isExecutable())
                        .findFirst().ifPresent(check -> {
                            check.vl = check.punishVl + 69;
                            check.punish();
                });
                cmd.getSender().sendMessage(Color.Green + "Force banned the player.");
            } else cmd.getSender().sendMessage(Color.Red + "Player not found.");
        } else cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("error-invalid-args", "&cInvalid arguments! Check the help page."));
    }
}
