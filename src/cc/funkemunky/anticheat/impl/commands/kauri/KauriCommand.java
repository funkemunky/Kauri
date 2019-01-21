package cc.funkemunky.anticheat.impl.commands.kauri;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.impl.commands.kauri.arguments.DebugArgument;
import cc.funkemunky.anticheat.impl.commands.kauri.arguments.LogArgument;
import cc.funkemunky.anticheat.impl.commands.kauri.arguments.RecentArgument;
import cc.funkemunky.anticheat.impl.commands.kauri.arguments.ReloadArgument;
import cc.funkemunky.api.commands.FunkeCommand;

public class KauriCommand extends FunkeCommand {
    public KauriCommand() {
        super(Kauri.getInstance(), "kauri", "Kauri", "The Kauri anticheat main command.", "kauri.command");
    }

    @Override
    protected void addArguments() {
        getArguments().add(new ReloadArgument(this, "reload", "reload", "reload the Kauri config.", "kauri.reload"));
        getArguments().add(new DebugArgument(this, "debug", "debug <check,none> [player]", "debug a check.", "kauri.debug"));
        getArguments().add(new LogArgument(this, "log", "log [clearlog, verbose] [args]", "view the log of a player.", "kauri.log"));
        getArguments().add(new RecentArgument(this, "recent", "recent", "see recently flagged players.", "mercury.recent"));
    }
}
