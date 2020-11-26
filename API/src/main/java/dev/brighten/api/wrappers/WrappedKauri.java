package dev.brighten.api.wrappers;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import dev.brighten.api.KauriAPI;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;

public class WrappedKauri extends Wrapper {

    public WrappedKauri(WrappedClass wclass, Object object) {
        super(wclass, object);
    }

    @SneakyThrows
    public WrappedDataManager getDataManager() {
        WrappedClass dataMClass = Reflections.getClass(KauriAPI.INSTANCE.object.getString("dataManager"));
        return new WrappedDataManager(dataMClass, wrappedClass.getFieldByType(dataMClass.getParent(), 0));
    }

    public Plugin getPlugin() {
        return (Plugin) object;
    }
}
