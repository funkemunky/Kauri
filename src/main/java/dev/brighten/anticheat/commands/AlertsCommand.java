package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;

@Init(commands =  true)
public class AlertsCommand {

    @Command(name = "kauri.alerts", description = "toggle off cheat alerts.", aliases = {"alerts"},
            display = "alerts", playerOnly = true, permission = "kauri.alerts")
    public void onCommand(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

        if(data != null) {
            data.alerts = !data.alerts;

            if(data.alerts) {
                Kauri.INSTANCE.dataManager.hasAlerts.add(data);
                cmd.getPlayer().sendMessage(Color.Green + "You are now viewing cheat alerts.");
            } else {
                Kauri.INSTANCE.dataManager.hasAlerts.remove(data);
                cmd.getPlayer().sendMessage(Color.Red + "You are no longer viewing cheat alerts.");
            }
        } else cmd.getSender().sendMessage(Color.Red + "There was an error trying to find your data.");
    }

}
