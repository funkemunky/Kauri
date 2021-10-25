package dev.brighten.anticheat.data;

import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.bukkit.entity.Player;

public class DataManager {
    public final Int2ObjectMap<ObjectData> dataMap = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    public final IntOpenHashSet hasAlerts = new IntOpenHashSet(),
            devAlerts = new IntOpenHashSet();

    public DataManager() {
    }

    public ObjectData getData(Player player) {
        return dataMap.get(player.getUniqueId().hashCode());

    }

    public void createData(Player player) {
        ObjectData data = new ObjectData(player.getUniqueId());

        dataMap.put(player.getUniqueId().hashCode(), data);
    }
}
