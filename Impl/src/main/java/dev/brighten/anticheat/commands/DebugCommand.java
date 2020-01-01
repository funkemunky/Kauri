package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.UUID;

@Init(commands = true)
public class DebugCommand {

    @Command(name = "kauri.debug", aliases = {"debug"}, permission = "kauri.debug",
            usage = "/<command> <check> [player]", display = "debug", description = "debug a check", playerOnly = true)
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

            if(data == null) {
                cmd.getPlayer().sendMessage(Color.Red + "There was an error trying to find your data object.");
                return;
            }
            UUID target = cmd.getArgs().length > 1
                    ? Bukkit.getOfflinePlayer(cmd.getArgs()[1]).getUniqueId() : cmd.getPlayer().getUniqueId();

            if(target != null) {
                if(Check.isCheck(cmd.getArgs()[0].replace("_", " "))) {
                    data.debugging = cmd.getArgs()[0].replace("_", " ");
                    data.debugged = target;

                    cmd.getPlayer().sendMessage(Color.Green + "You are now debugging " + data.debugging
                            + " on target " + Bukkit.getOfflinePlayer(target).getName() + "!");
                } else cmd.getPlayer()
                        .sendMessage(Color.Red + "The argument input \"" + cmd.getArgs()[0] + "\" is not a check.");
            } else cmd.getPlayer().sendMessage(Color.Red + "Could not find a target to debug.");
        } else cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("error-invalid-args", "&cInvalid arguments! Check the help page."));
    }

    @Command(name = "kauri.debug.none", aliases = {"debug.none"}, permission = "kauri.debug", usage = "/<command>",
            playerOnly = true, display = "debug none", description = "turn off debugging")
    public void onDebugOff(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

        if(data == null) {
            cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                    .msg("error-data-object", "&cThere was an error trying to find your data object."));
            return;
        }

        data.debugging = null;
        data.debugged = null;
        Kauri.INSTANCE.dataManager.debugging.remove(data);

        Kauri.INSTANCE.dataManager.dataMap.values().stream()
                .filter(d -> d.boxDebuggers.contains(cmd.getPlayer()))
                .forEach(d -> d.boxDebuggers.remove(cmd.getPlayer()));
        cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-off", "&aTurned off your debugging."));
    }

    @Command(name = "kauri.debug.box", aliases = {"debug.box"}, permission = "kauri.debug", usage = "/<command>",
            playerOnly = true, display = "debug box [player...]", description = "debug the collisions of players.")
    public void onDebugBox(CommandAdapter cmd) {
        String[] debuggingPlayers;
        ObjectData.debugBoxes(false, cmd.getPlayer());
        if(cmd.getArgs().length == 0) {
            ObjectData.debugBoxes(true, cmd.getPlayer(), cmd.getPlayer().getUniqueId());
            debuggingPlayers = new String[] {cmd.getPlayer().getName()};
        } else {
            ObjectData.debugBoxes(true, cmd.getPlayer(),
                    (debuggingPlayers = Arrays.copyOfRange(cmd.getArgs(), 1, cmd.getArgs().length - 1)));
        }

        cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage()
                .msg("debug-boxes", "&aYou are now debugging the collisions of %players%.")
        .replace("%players%", String.join(",", debuggingPlayers)));

    }

}
