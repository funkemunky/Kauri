package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;

public class UpdateConfigArgument extends FunkeArgument {

    public UpdateConfigArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if(args.length > 2) {
            if(args[1].equalsIgnoreCase("messages")) {

            } else if(args[1].equalsIgnoreCase("config")) {
                val instance = Kauri.getInstance();
                val config = instance.getConfig();
                switch(args[1].toLowerCase()) {
                    case "full":
                        sender.sendMessage(Color.translate("&7Config Updater started! Updating: &fFull Config&7."));
                        File file = new File(Kauri.getInstance().getDataFolder(), "config.yml");
                        sender.sendMessage(Color.translate("&7Deleting file..."));
                        file.delete();
                        sender.sendMessage(Color.translate("&7Generating new config..."));
                        instance.saveDefaultConfig();
                        sender.sendMessage(Color.translate("&aCompleted!"));
                        break;
                    case "checks":
                        sender.sendMessage(Color.translate("&7Config Updater started! Updating: &fChecks&7."));
                        sender.sendMessage(Color.translate("&7Deleting checks section..."));
                        config.set("checks", null);
                        instance.saveConfig();
                        sender.sendMessage(Color.translate("&7Reloading Kauri..."));
                        Kauri.getInstance().reloadKauri();
                        sender.sendMessage(Color.translate("&aCompleted!"));
                        break;
                    case "pup":
                        sender.sendMessage(Color.translate("&7Config Updater started! Updating: &fPuP&7."));
                        sender.sendMessage(Color.translate("&7Deleting pup section..."));
                        config.set("antipup", null);
                        instance.saveConfig();
                        sender.sendMessage(Color.translate("&7Reloading Kauri..."));
                        Kauri.getInstance().reloadKauri();
                        sender.sendMessage(Color.translate("&aCompleted!"));
                        break;
                    default:
                        sender.sendMessage(Color.translate(Messages.invalidArguments));
                        break;
                }
            } else {
                sender.sendMessage(Color.translate(Messages.invalidArguments));
            }
        } else {
            sender.sendMessage(Color.translate(Messages.invalidArguments));
        }
    }
}
