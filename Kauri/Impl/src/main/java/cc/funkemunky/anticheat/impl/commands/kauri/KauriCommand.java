package cc.funkemunky.anticheat.impl.commands.kauri;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.impl.commands.kauri.arguments.*;
import cc.funkemunky.anticheat.impl.listeners.CustomListeners;
import cc.funkemunky.anticheat.impl.listeners.ImportantListeners;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.commands.ancmd.SpigotCommand;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.MiscUtils;

import java.util.Arrays;

public class KauriCommand extends FunkeCommand {
    public KauriCommand() {
        super(Kauri.getInstance(), "kauri", "Kauri", "The Anticheat main command.", "kauri.command", true);

        if(CustomListeners.isAllowed) {
            setName(ImportantListeners.mainCommand);
            setDisplay(ImportantListeners.mainDisplay);
        }
        SpigotCommand command = new SpigotCommand(getName(), this, Kauri.getInstance(), true);
        Atlas.getInstance().getCommandManager().getMap().register(Kauri.getInstance().getName(), command);
    }

    @Override
    protected void addArguments() {
        getArguments().add(new ReloadArgument(this, "reload", "reload", "reload the Kauri config.", "kauri.reload"));
        getArguments().add(new DebugArgument(this, "debug", "debug <check,none> [player]", "debug a check.", "kauri.debug"));
        getArguments().add(new LagArgument(this, "lag", "lag <profile,server,player> [args]", "view extensive lag information.", "kauri.lag"));
        getArguments().add(new MenuArgument(this, "menu", "menu", "open check editor.", "kauri.menu"));
        getArguments().add(new BugReportArg(this, "bugreport", "bugreport <config,info>", "use when making a bug report.", "kauri.bugreport"));
        getArguments().add(new DelayArgument(this, "delay", "delay <ms>", "set the delay of the alerts.", "kauri.delay"));
        getArguments().add(new AlertsArgument(this, "alerts", "alerts [dev]", "toggle your alerts", "kauri.alerts", "kauri.staff"));
        getArguments().add(new SaveArgument(this, "save", "save", "save all data", "kauri.save"));
        getArguments().add(new LogArgument(this, "logs", "logs <player>", "view the logs of a player", "kauri.logs"));
        getArguments().add(new RecentViolationsArgument(this, "recent", "recent", "view the recent violators.", "kauri.recent"));
        getArguments().add(new UpdateConfigArgument(this, "updateconfig", "updateConfig <config/messages> [args]", "update certain or all parts of a config", "kauri.updateConfig"));
        getArguments().add(new BoxWandArgument(this, "boxwand", "boxwand", "receive the magic box wand.", "kauri.boxwand"));
        getArguments().add(new BypassingArgument(this, "bypass", "bypass <player> [boolean]", "set a player to bypass detection without permissions.", "kauri.bypass.command"));
        getArguments().add(new AntiVpnArgument(this, "antivpn", "antivpn <args>", "manage the antivpn.", "kauri.antivpn"));
        getArguments().add(new BanwaveArgument(this, "banwave", "banwave", "force a ban wave to run.", "kauri.banwave"));
        getArguments().add(new VelocityArgument(this, "velocity", "velocity <xz> <y>", "apply velocity.", "kauri.velocity"));
        getArguments().add(new AdminArgument(this, "admin", "admin <arg> [args]", "admin stuff.", "kauri.admin"));

        getArguments().forEach(argument -> {
            Arrays.stream(argument.getClass().getDeclaredFields()).filter(field -> field.getAnnotations().length > 0).forEach(field -> {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Message.class)) {
                    try {
                        Message msg = field.getAnnotation(Message.class);

                        MiscUtils.printToConsole("&eFound " + field.getName() + " Message (default=" + field.get(argument) + ").");
                        if (Kauri.getInstance().getMessages().get(msg.name()) != null) {
                            MiscUtils.printToConsole("&eValue not found in message configuration! Setting default into messages.yml...");
                            field.set(argument, Kauri.getInstance().getMessages().getString(msg.name()));
                        } else {
                            Kauri.getInstance().getMessages().set(msg.name(), field.get(argument));
                            Kauri.getInstance().saveMessages();
                            MiscUtils.printToConsole("&eValue found in message configuration! Set value to &a" + Kauri.getInstance().getConfig().get(msg.name()));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else if (field.isAnnotationPresent(ConfigSetting.class)) {
                    ConfigSetting annotation = field.getAnnotation(ConfigSetting.class);
                    String name = annotation.name();
                    String path = annotation.path() + "." + (name.length() > 0 ? name : field.getName());
                    try {
                        MiscUtils.printToConsole("&eFound " + field.getName() + " ConfigSetting (default=" + field.get(argument) + ").");
                        if (Kauri.getInstance().getConfig().get(path) == null) {
                            MiscUtils.printToConsole("&eValue not found in configuration! Setting default into config...");
                            Kauri.getInstance().getConfig().set(path, field.get(argument));
                            Kauri.getInstance().saveConfig();
                        } else {
                            field.set(argument, Kauri.getInstance().getConfig().get(path));

                            MiscUtils.printToConsole("&eValue found in configuration! Set value to &a" + Kauri.getInstance().getConfig().get(path));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }
}
