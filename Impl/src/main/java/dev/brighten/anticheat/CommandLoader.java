package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;

@Init
public class CommandLoader {

    public CommandLoader() {
        MiscUtils.printToConsole(Color.Gray + "Loading commands...");
        Kauri.INSTANCE.commandManager = Atlas.getInstance().getBukkitCommandManager(Kauri.INSTANCE);
        Kauri.INSTANCE.commandManager.enableUnstableAPI("help");
    }
}
