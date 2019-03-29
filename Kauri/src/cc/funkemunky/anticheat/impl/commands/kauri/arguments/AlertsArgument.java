package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlertsArgument extends FunkeArgument {
    public AlertsArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("toggleAlerts");
        addAlias("ta");
        addAlias("talerts");

        addTabComplete(2, "dev");

        setPlayerOnly(true);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(((Player) sender).getUniqueId());

        if (data == null) {
            sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "There was an error trying to find your data object.");
            return;
        }

        data.setAlertsEnabled(!data.isAlertsEnabled());
        if(args.length > 1 && args[1].equalsIgnoreCase("dev")) {
            data.setDeveloperAlerts(data.isAlertsEnabled());
            sender.sendMessage(Color.translate("&7Toggled your alerts with developer mode &f" + (data.isAlertsEnabled() ? "on" : "off") + "&7."));
        } else {
            sender.sendMessage(Color.translate("&7Toggled your alerts &f" + (data.isAlertsEnabled() ? "on" : "off") + "&7."));
        }
    }
}
