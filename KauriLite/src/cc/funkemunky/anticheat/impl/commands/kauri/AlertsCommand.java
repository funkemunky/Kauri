package cc.funkemunky.anticheat.impl.commands.kauri;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import org.bukkit.entity.Player;

@Init(commands = true)
public class AlertsCommand {

    @Command(name = "alerts", description = "Toggle the alerts for Kauri.", aliases = {"togglealerts", "talerts", "ta"}, permission = "kauri.alerts", playerOnly = true)
    public void onAlerts(CommandAdapter command) {
        Player player = command.getPlayer();
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(player.getUniqueId());
        String[] args = command.getArgs();

        if (data == null) {
            player.sendMessage(Color.Red + "There was an error trying to find your data object.");
            return;
        }

        data.setAlertsEnabled(!data.isAlertsEnabled());
        if(args.length > 0 && args[0].equalsIgnoreCase("dev")) {
            data.setDeveloperAlerts(data.isAlertsEnabled());
            player.sendMessage(Color.translate("&7Toggled your alerts with developer mode &f" + (data.isAlertsEnabled() ? "on" : "off") + "&7."));
        } else {
            player.sendMessage(Color.translate("&7Toggled your alerts &f" + (data.isAlertsEnabled() ? "on" : "off") + "&7."));
        }
    }
}
