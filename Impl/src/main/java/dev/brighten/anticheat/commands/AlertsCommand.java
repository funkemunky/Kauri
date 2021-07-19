package dev.brighten.anticheat.commands;

import cc.funkemunky.api.utils.Init;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.entity.Player;

@Init
@CommandAlias("alerts")
@CommandPermission("kauri.command.alerts")
public class AlertsCommand extends BaseCommand {

    @Default
    @Syntax("")
    @Description("Toggle your cheat alerts")
    public void onCommand(Player player) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data != null) {
            if(data.alerts = !data.alerts) {
                Kauri.INSTANCE.dataManager.hasAlerts.add(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-on",
                        "&aYou are now viewing cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.hasAlerts.remove(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-none",
                        "&cYou are no longer viewing cheat alerts."));
            }
            Kauri.INSTANCE.loggerManager.storage.updateAlerts(data.getUUID(), data.alerts);
        } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

    @Subcommand("dev")
    @Syntax("")
    @CommandPermission("kauri.command.alerts.dev")
    @Description("Toggle developer cheat alerts")
    public void onDevAlertsMain(Player player) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(player);

        if(data != null) {
            if(data.devAlerts = !data.devAlerts) {
                Kauri.INSTANCE.dataManager.devAlerts.add(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-on",
                        "&aYou are now viewing developer cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.devAlerts.remove(data);
                player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-none",
                        "&cYou are no longer viewing developer cheat alerts."));
            }
            Kauri.INSTANCE.loggerManager.storage.updateDevAlerts(data.getUUID(), data.devAlerts);
        } else player.sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }
}
