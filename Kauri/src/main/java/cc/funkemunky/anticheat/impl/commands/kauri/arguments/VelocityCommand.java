package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import lombok.var;
import org.bukkit.util.Vector;

@Init(commands = true)
public class VelocityCommand {

    @Command(name = "velocity", playerOnly = true, description = "Apply velocity to a player", permission = "kauri.velocity")
    public void onCommand(CommandAdapter command) {
        command.getSender().sendMessage(Color.Red + "Args: " + "/velocity <dir,custom> <args>");
    }

    @Command(name = "velocity.custom", playerOnly = true, description = "Apply velocity.", permission = "kauri.velocity")
    public void onCustom(CommandAdapter command) {
        if(command.getArgs().length > 2) {
            val args = command.getArgs();
            val sender = command.getPlayer();
            try {
                double x = Double.parseDouble(args[0]), y = Double.parseDouble(args[1]), z = Double.parseDouble(args[2]);

                val vec = new Vector(x, y, z);

                sender.setVelocity(vec);
                sender.sendMessage(Color.Green + "Applied velocity!");
            } catch(NumberFormatException e) {
                sender.sendMessage(Color.Red + "You must input all arguments in the form of a double.");
            }
        } else {
            command.getSender().sendMessage(Color.Red + "Args: " + "/velocity custom <x> <y> <z>");
        }
    }

    @Command(name = "velocity.dir", playerOnly = true, description = "Apply velocity by direction.", permission = "kauri.velocity")
    public void onDir(CommandAdapter command) {
        if(command.getArgs().length > 1) {
            val sender = command.getPlayer();

            try {
                double horz = Double.parseDouble(command.getArgs()[0]), vert = Double.parseDouble(command.getArgs()[1]);

                var direction = command.getPlayer().getLocation().getDirection().clone();

                direction = direction.setX(direction.getX() * horz).setY(0.45 * vert).setZ(direction.getZ() * horz);

                sender.setVelocity(direction);
                sender.sendMessage(Color.Green + "Applied velocity!");
            } catch(NumberFormatException e) {
                sender.sendMessage(Color.Red + "You must input all arguments in the form of a double.");
            }
        }else {
            command.getSender().sendMessage(Color.Red + "Args: " + "/velocity dir <horz> <vert>");
        }
    }
}
