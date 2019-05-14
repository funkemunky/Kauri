package cc.funkemunky.anticheat.impl.commands.kauri;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Init(commands = true)
public class AlertsCommand {

    @Command(name = "alerts", description = "Toggle the alerts for Kauri.", aliases = {"togglealerts", "talerts", "ta"}, permission = {"kauri.alerts", "kauri.staff"}, playerOnly = true)
    public void onAlerts(CommandAdapter command) {
        Player player = command.getPlayer();
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(player.getUniqueId());
        String[] args = command.getArgs();

        if (data == null) {
            player.sendMessage(Color.translate(Messages.errorData));
            return;
        }

        if(args.length > 0) {
            if (args[0].equalsIgnoreCase("dev")) {
                data.setDeveloperAlerts(!data.isDeveloperAlerts());
                data.setAlertsEnabled(true);
                player.sendMessage(Color.translate(Messages.toggledDevAlerts.replace("%enabled%", (data.isDeveloperAlerts() ? "on" : "off"))));
            } else if(Arrays.stream(AlertTier.values()).anyMatch(tier -> tier.getName().equalsIgnoreCase(args[0]))) {
                AlertTier tier = AlertTier.valueOf(args[0]);
                data.setAlertTier(tier);
                data.setAlertsEnabled(true);
                player.sendMessage(Color.translate(Messages.setTierAlerts.replace("%tier%", tier.getName())));
            } else if(args[0].equalsIgnoreCase("off")) {
                data.setAlertsEnabled(false);
                player.sendMessage(Color.translate(Messages.toggledAlerts.replace("%enabled%", "off")));
            } else {
                player.sendMessage(Color.translate(Messages.invalidArguments));
                player.sendMessage(Color.translate("&7Options: &f&odev&7, &f&olow&7, &f&olikely77, &f&ohigh&7, &f&ooff"));
            }
        } else player.sendMessage(Color.translate(Messages.invalidArguments));
    }
}
