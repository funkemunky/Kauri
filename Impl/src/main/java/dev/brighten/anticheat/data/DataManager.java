package dev.brighten.anticheat.data;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataManager {
    public Map<UUID, ObjectData> dataMap = new ConcurrentHashMap<>();
    public List<ObjectData> hasAlerts = new CopyOnWriteArrayList<>(), devAlerts = new CopyOnWriteArrayList<>(),
            debugging = new CopyOnWriteArrayList<>();

    public DataManager() {
        RunUtils.taskTimerAsync(() -> {
            hasAlerts.clear();
            dataMap.values().stream().filter(data -> data.alerts).forEach(hasAlerts::add);
            debugging.clear();
            dataMap.values().stream().filter(data -> data.debugging != null).forEach(debugging::add);
        }, Kauri.INSTANCE, 60L, 30L);
    }

    public ObjectData getData(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(),
                ObjectData::new);
    }

    public void removeData(UUID player) {
        dataMap.remove(player);
    }
}
