package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
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

        addTabComplete(2, "messages", "config");
        addTabComplete(3, "full", "checks", "pup", "antipup");
    }

    @Message(name = "command.updateConfig.started")
    private String started = "&7Config Updater started! Updating: &f%type%&7.";

    @Message(name = "command.updateConfig.deletingFile")
    private String deletingFile = "&7Deleting file...";

    @Message(name = "command.updateConfig.gen.newConfig")
    private String genNewConfig = "&7Generating new config...";

    @Message(name = "command.updateConfig.completed")
    private String completed = "&aCompleted!";

    @Message(name = "command.updateConfig.reloading")
    private String reloadingKauri = "&7Reloading Kauri...";

    @Message(name = "command.updateConfig.deletingSection")
    private String deletingSection = "&7Deleting %section% section...";

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if(args.length > 2) {
            if(args[1].equalsIgnoreCase("messages")) {
                sender.sendMessage(Color.translate(started.replace("%type%", "Messages")));
                sender.sendMessage(Color.translate(deletingFile));
                Kauri.getInstance().getMessagesFile().delete();
                sender.sendMessage(Color.translate(genNewConfig));
                Kauri.getInstance().saveDefaultMessages();
                sender.sendMessage(Color.translate(completed));
            } else if(args[1].equalsIgnoreCase("config")) {
                val instance = Kauri.getInstance();
                val config = instance.getConfig();
                switch(args[2].toLowerCase()) {
                    case "full":
                        sender.sendMessage(Color.translate(started.replace("%type%", "Full Config")));
                        File file = new File(Kauri.getInstance().getDataFolder(), "config.yml");
                        sender.sendMessage(Color.translate(deletingFile));
                        file.delete();
                        sender.sendMessage(Color.translate(genNewConfig));
                        instance.saveDefaultConfig();
                        sender.sendMessage(Color.translate(reloadingKauri));
                        Kauri.getInstance().reloadKauri();
                        sender.sendMessage(Color.translate(completed));
                        break;
                    case "checks":
                        sender.sendMessage(Color.translate(started.replace("%type%", "Checks")));
                        sender.sendMessage(Color.translate(deletingSection.replace("%section%", "checks")));
                        config.set("checks", null);
                        instance.saveConfig();
                        sender.sendMessage(Color.translate(reloadingKauri));
                        Kauri.getInstance().reloadKauri();
                        sender.sendMessage(Color.translate(completed));
                        break;
                    case "pup":
                    case "antipup":
                        sender.sendMessage(Color.translate(started.replace("%type%", "AntiPuP")));
                        sender.sendMessage(Color.translate(deletingSection.replace("%section%", "antipup")));
                        config.set("antipup", null);
                        instance.saveConfig();
                        sender.sendMessage(Color.translate(reloadingKauri));
                        Kauri.getInstance().reloadKauri();
                        sender.sendMessage(Color.translate(completed));
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
