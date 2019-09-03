package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import lombok.val;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AdminArgument extends FunkeArgument {
    public AdminArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addTabComplete(3, Kauri.getInstance().getCheckManager().getChecks().stream()
                .map(check -> check.getName().replace(" ", "_"))
                .collect(Collectors.toList())
                .toArray(new String[] {}));
    }

    @Override
    public void onArgument(CommandSender commandSender, Command command, String[] args) {
        if(args.length > 1) {
            switch(args[1].toLowerCase()) {
                case "clearlogs": {
                    if(args.length > 2) {
                        commandSender.sendMessage(Color.Gray + "Clearing logs...");
                        for (UUID uuid : Kauri.getInstance().getLoggerManager().getViolations().keySet()) {
                            val map = new ArrayList<>(Kauri.getInstance().getLoggerManager().getViolations().get(uuid));

                            val shit = map.stream().filter(vio -> vio != null && vio.getCheckName() != null && !vio.getCheckName().equalsIgnoreCase(args[2].replace("_", " "))).collect(Collectors.toList());

                            Kauri.getInstance().getLoggerManager().getViolations().put(uuid, shit);
                        }
                        commandSender.sendMessage(Color.Gray + "Saving...");
                        Kauri.getInstance().getLoggerManager().saveToDatabase();
                        commandSender.sendMessage(Color.Green + "Done!");
                    } else commandSender.sendMessage(Color.translate(Messages.invalidArguments));
                    break;
                }
                default: {
                    commandSender.sendMessage(Color.translate(Messages.invalidArguments));
                    break;
                }
            }
        } else commandSender.sendMessage(Color.translate(Messages.invalidArguments));
    }
}
