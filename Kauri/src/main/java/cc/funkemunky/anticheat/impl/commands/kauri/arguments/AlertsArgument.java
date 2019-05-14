package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AlertsArgument extends FunkeArgument {
    public AlertsArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("toggleAlerts");
        addAlias("ta");
        addAlias("talerts");

        addTabComplete(2, "dev", "off");
        addTabComplete(2, Arrays.stream(AlertTier.values()).filter(tier -> tier.getPriority() < 4).map(AlertTier::getName).collect(Collectors.toList()).toArray(new String[AlertTier.values().length]));

        setPlayerOnly(true);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(((Player) sender).getUniqueId());

        if (data == null) {
            sender.sendMessage(Color.translate(Messages.errorData));
            return;
        }

        Player player = (Player) sender;

        if(args.length > 1) {
            if (args[1].equalsIgnoreCase("dev")) {
                data.setDeveloperAlerts(!data.isDeveloperAlerts());
                data.setAlertsEnabled(true);
                player.sendMessage(Color.translate(Messages.toggledDevAlerts.replace("%enabled%", (data.isDeveloperAlerts() ? "on" : "off"))));
            } else if(Arrays.stream(AlertTier.values()).anyMatch(tier -> tier.getName().equalsIgnoreCase(args[1]))) {
                AlertTier tier = AlertTier.valueOf(args[1]);
                data.setAlertTier(tier);
                data.setAlertsEnabled(true);
                player.sendMessage(Color.translate(Messages.setTierAlerts.replace("%tier%", tier.getName())));
            } else if(args[1].equalsIgnoreCase("off")) {
                data.setAlertsEnabled(false);
                player.sendMessage(Color.translate(Messages.toggledAlerts.replace("%enabled%", "off")));
            } else {
                player.sendMessage(Color.translate(Messages.invalidArguments));
                player.sendMessage(Color.translate("&7Options: &f&odev&7, &f&olow&7, &f&olikely77, &f&ohigh&7, &f&ooff"));
            }
        } else player.sendMessage(Color.translate(Messages.invalidArguments));
    }
}
