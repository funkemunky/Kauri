package dev.brighten.api.wrappers;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import dev.brighten.api.data.Data;
import org.bukkit.entity.Player;

public class WrappedDataManager extends Wrapper {

    private static WrappedClass objectdataClass = Reflections.getClass("dev.brighten.anticheat.data.ObjectData");

    public WrappedDataManager(Object object) {
        super(Reflections.getClass("dev.brighten.anticheat.data.DataManager"), object);
    }

    public Data getData(Player player) {
        return fetchMethod("getData", player);
    }

    public void createData(Player player) {
        fetchMethod("createData", player);
    }
}
