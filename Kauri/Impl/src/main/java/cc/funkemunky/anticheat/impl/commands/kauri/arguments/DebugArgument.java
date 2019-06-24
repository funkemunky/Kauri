package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DebugArgument extends FunkeArgument {

    public DebugArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addTabComplete(2, "none");
        addTabComplete(2, "box");
        addTabComplete(2, "packets");
        List<String> checks = new ArrayList<>();
        Kauri.getInstance().getCheckManager().getChecks().forEach(check -> checks.add(check.getName().replaceAll(" ", "_")));

        String[] checkArray = new String[checks.size()];

        for (int i = 0; i < checks.size(); i++) {
            checkArray[i] = checks.get(i);
        }
        addTabComplete(2, checkArray);

        setPlayerOnly(true);
    }

    @Message(name = "command.debug.boxDebug")
    private String boxDebug = "&7Set box debug to: &f%debugging%";

    @Message(name = "command.debug.stoppedDebug")
    private String stoppedDebug = "&cStopped any debug messages from being sent to you.";

    @Message(name = "command.debug.debugPacketsSender")
    private String packetsSender = "&7You are now debugging your packets.";

    @Message(name = "command.debug.debugPacketsTarget")
    private String packetsTarget = "&7You are not debugging &f%target%&7's packets.";

    @Message(name = "command.debug.debugCheckSender")
    private String checkSender = "&7You are now debugging &f%check% &7for yourself.";

    @Message(name = "command.debug.debugCheckTarget")
    private String checkTarget = "&7You are now debugging &f%check% for &f%target%&7.";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        Player player = (Player) sender;
        PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(player.getUniqueId());

        if (data != null) {
            if (args[1].equalsIgnoreCase("box")) {
                data.setDebuggingBox(!data.isDebuggingBox());
                sender.sendMessage(Color.translate(boxDebug.replace("%debugging%", String.valueOf(data.isDebuggingBox()))));
            } else if (args[1].equalsIgnoreCase("none")) {
                data.setDebuggingCheck(null);
                data.setDebuggingPackets(false);
                data.setDebuggingPlayer(null);
                sender.sendMessage(Color.translate(stoppedDebug));
            } else if (args[1].equalsIgnoreCase("packets")) {
                if (args.length == 3) {
                    data.setDebuggingPlayer(player.getUniqueId());
                    data.setDebuggingPackets(true);
                    data.setSpecificPacketDebug(args[2]);

                    sender.sendMessage(Color.translate(packetsSender));
                } else {
                    Player target = Bukkit.getPlayer(args[2]);

                    if (target != null) {
                        data.setDebuggingPlayer(target.getUniqueId());
                        data.setDebuggingPackets(true);
                        data.setSpecificPacketDebug(args[3]);
                        sender.sendMessage(Color.translate(packetsTarget.replace("%target%", target.getName())));
                    } else {
                        sender.sendMessage(Color.translate(Messages.errorPlayerOffline));
                    }
                }
            } else {
                Check check = Kauri.getInstance().getCheckManager().getCheck(args[1].replaceAll("_", " "));

                if (check == null) {
                    sender.sendMessage(Color.translate(Messages.checkDoesntExist.replace("%check%", args[1])));
                    return;
                }

                if (args.length == 2) {
                    data.setDebuggingPlayer(player.getUniqueId());
                    data.setDebuggingCheck(check);

                    sender.sendMessage(Color.translate(checkSender.replace("%check%", check.getName())));
                } else {
                    Player target = Bukkit.getPlayer(args[2]);

                    if (target != null) {
                        data.setDebuggingPlayer(target.getUniqueId());
                        data.setDebuggingCheck(check);
                        sender.sendMessage(Color.translate(checkTarget.replace("%target%", target.getName()).replace("%check%", check.getName())));
                    } else {
                        sender.sendMessage(Color.translate(Messages.errorPlayerOffline));
                    }
                }
            }
        } else {
            sender.sendMessage(Color.translate(Messages.errorData));
        }
    }
}
