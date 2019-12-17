package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;

import java.lang.reflect.Field;

@Init(commands = true)
public class DelayCommand {

    private static Field field = new WrappedClass(Config.class).getFieldByName("alertsDelay").getField();

    @Command(name = "kauri.delay", description = "change the delay between alerts.", display = "delay [ms]",
            permission = "kauri.delay", aliases = {"delay"}, usage = "/<command> <ms>")
    public void onCommand(CommandAdapter cmd) {
        if(cmd.getArgs().length > 0) {
            try {
                long delay = Long.parseLong(cmd.getArgs()[0]);
                cmd.getSender().sendMessage(Color.Gray + "Setting delay to "
                        + Color.White + delay + "ms" + Color.Gray + "...");

                Config.alertsDelay = delay;
                ConfigSetting setting = field.getAnnotation(ConfigSetting.class);

                Kauri.INSTANCE.getConfig().set(setting.path() + "." + setting.name(), delay);
                Kauri.INSTANCE.saveConfig();
                cmd.getSender().sendMessage(Color.Green + "Delay set!");
            } catch(NumberFormatException e) {
                cmd.getSender().sendMessage(Color.Red + "The argument \"" + cmd.getArgs()[0]
                        + "\" provided is not a long.");
            }
        } else cmd.getSender()
                .sendMessage(Color.Red + "Invalid arguments. Check the help page for more information.");
    }
}
