package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;

@Init(commands =  true)
public class AlertsCommand {

    @Command(name = "kauri.alerts", description = "toggle on/off cheat alerts.", aliases = {"alerts"},
            display = "alerts", playerOnly = true, permission = "kauri.command.alerts")
    public void onCommand(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

        if(data != null) {
            if(data.alerts = !data.alerts) {
                Kauri.INSTANCE.dataManager.hasAlerts.add(data);
                cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-on",
                        "&aYou are now viewing cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.hasAlerts.remove(data);
                cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-none",
                        "&cYou are no longer viewing cheat alerts."));
            }
        } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

    @Command(name = "kauri.alerts.dev", description = "toggle on/off dev cheat alerts.", aliases = {"alerts.dev"},
            display = "alerts dev", playerOnly = true, permission = "kauri.command.alerts.dev")
    public void onAlertsDevCommand(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

        if(data != null) {
            if(data.devAlerts = !data.devAlerts) {
                Kauri.INSTANCE.dataManager.devAlerts.add(data);
                cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-on",
                        "&aYou are now viewing developer cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.devAlerts.remove(data);
                cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("dev-alerts-none",
                        "&cYou are no longer viewing developer cheat alerts."));
            }
        } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

}
