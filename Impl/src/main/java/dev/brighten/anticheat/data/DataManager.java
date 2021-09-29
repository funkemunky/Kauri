package dev.brighten.anticheat.data;

import gnu.trove.impl.sync.TSynchronizedIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.bukkit.entity.Player;

import java.util.*;

public class DataManager {
    public final TSynchronizedIntObjectMap<ObjectData> dataMap = new TSynchronizedIntObjectMap<>(new TIntObjectHashMap<>());
    public final TIntHashSet hasAlerts = new TIntHashSet(),
            devAlerts = new TIntHashSet();

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
