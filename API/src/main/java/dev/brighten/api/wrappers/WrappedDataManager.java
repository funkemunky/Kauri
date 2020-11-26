package dev.brighten.api.wrappers;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import dev.brighten.api.KauriAPI;
import dev.brighten.api.data.Data;
import dev.brighten.db.utils.json.JSONException;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

public class WrappedDataManager extends Wrapper {

    private static WrappedClass objectdataClass;

    static {
        try {
            objectdataClass = Reflections.getClass(KauriAPI.INSTANCE.object.getString("objectData"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public WrappedDataManager(WrappedClass wclass, Object object) {
        super(wclass, object);
    }

    public Data getData(Player player) {
        return wrappedClass.getMethodByType(objectdataClass.getParent(), 0).invoke(object, player);
    }

    public void createData(Player player) {
        wrappedClass.getMethodByType(Void.class, 0).invoke(object, player);
    }
}
