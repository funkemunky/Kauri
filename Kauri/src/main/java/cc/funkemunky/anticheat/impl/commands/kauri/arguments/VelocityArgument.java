package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityArgument extends FunkeArgument {
    public VelocityArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        setPlayerOnly(true);
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if(args.length == 3) {
            try {
                Player player = (Player) sender;
                double xz = Double.parseDouble(args[1]), y = Double.parseDouble(args[2]);

                Vector direction = player.getEyeLocation().getDirection();

                direction.setY(0);
                direction.multiply(xz);
                direction.setY(y);

                player.setVelocity(direction);

                sender.sendMessage(Color.Green + "Applied velocity: (" + direction.lengthSquared() + ", " + y + ") input=(" + xz + ", " + y + ")");
            } catch(NumberFormatException e) {
                sender.sendMessage(Color.Red + "Ensure your input is in the form of a number.");
            }
        } else {
            sender.sendMessage(Color.translate(Messages.invalidArguments));
        }
    }
}
