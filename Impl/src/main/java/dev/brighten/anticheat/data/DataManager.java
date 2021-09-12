package dev.brighten.anticheat.data;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataManager {
    public Map<UUID, ObjectData> dataMap = new ConcurrentHashMap<>();
    public Set<ObjectData> hasAlerts = Collections.synchronizedSet(new HashSet<>()),
            devAlerts = Collections.synchronizedSet(new HashSet<>()),
            debugging = Collections.synchronizedSet(new HashSet<>());

    public DataManager() {
        RunUtils.taskTimerAsync(() -> {
            hasAlerts.clear();
            dataMap.values().stream().filter(data -> data.alerts).forEach(hasAlerts::add);
            debugging.clear();
            dataMap.values().stream().filter(data -> data.debugging != null).forEach(debugging::add);
        }, Kauri.INSTANCE, 60L, 30L);
    }

    public ObjectData getData(Player player) {
        return dataMap.getOrDefault(player.getUniqueId(), null);
    }

    public void createData(Player player) {
        ObjectData data = new ObjectData(player.getUniqueId());

        dataMap.put(player.getUniqueId(), data);
    }
}
