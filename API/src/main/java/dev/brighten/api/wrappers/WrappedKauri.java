package dev.brighten.api.wrappers;

import cc.funkemunky.api.reflections.Reflections;
import org.bukkit.plugin.Plugin;

public class WrappedKauri extends Wrapper {

    public WrappedKauri(Object object) {
        super(Reflections.getClass("dev.brighten.anticheat.Kauri"), object);
    }

    public WrappedDataManager getDataManager() {
        return new WrappedDataManager(fetchField("dataManager"));
    }

    public Plugin getPlugin() {
        return (Plugin) object;
    }
}
