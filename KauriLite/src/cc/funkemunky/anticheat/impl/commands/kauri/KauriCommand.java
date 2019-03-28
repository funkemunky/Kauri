package cc.funkemunky.anticheat.impl.commands.kauri;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.impl.commands.kauri.arguments.*;
import cc.funkemunky.api.commands.FunkeCommand;

public class KauriCommand extends FunkeCommand {
    public KauriCommand() {
        super(Kauri.getInstance(), "kauri", "Kauri", "The Kauri anticheat main command.", "kauri.command");
    }

    @Override
    protected void addArguments() {
        getArguments().add(new ReloadArgument(this, "reload", "reload", "reload the Kauri config.", "kauri.reload"));
        getArguments().add(new DebugArgument(this, "debug", "debug <check,none> [player]", "debug a check.", "kauri.debug"));
        getArguments().add(new LagArgument(this, "lag", "lag <profile,server,player> [args]", "view extensive lag information.", "kauri.lag"));
        getArguments().add(new MenuArgument(this, "menu", "menu", "open check editor.", "kauri.menu"));
        getArguments().add(new BugReportArg(this, "bugreport", "bugreport <config,info>", "use when making a bug report.", "kauri.bugreport"));
        getArguments().add(new DelayArgument(this, "delay", "delay <ms>", "set the delay of the alerts.", "kauri.delay"));
        getArguments().add(new AlertsArgument(this, "alerts", "alerts", "toggle your alerts", "kauri.alerts"));
        getArguments().add(new SaveArgument(this, "save", "save", "save all data", "kauri.save"));
        getArguments().add(new LogArgument(this, "logs", "logs <player>", "view the logs of a player", "kauri.logs"));
        getArguments().add(new BoxWandArgument(this, "boxwand", "boxwand", "receive the magic box wand.", "kauri.boxwand"));
        getArguments().add(new BypassingArgument(this, "bypass", "bypass <player> [boolean]", "set a player to bypass detection without permissions.", "kauri.bypass.command"));
        getArguments().add(new BanwaveArgument(this, "banwave", "banwave", "force a ban wave to run.", "kauri.banwave"));
    }
}
