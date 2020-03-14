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

        RunUtils.taskTimerAsync(() -> dataMap.values().stream()
                        .filter(data -> data.target != null)
                        .forEach(data -> data.targetPastLocation.addLocation(data.target.getLocation())),
                Kauri.INSTANCE, 1L,
                1L);
    }

    public ObjectData getData(Player player) {
        return dataMap.getOrDefault(player.getUniqueId(), null);
    }

    public void createData(Player player) {
        ObjectData data = new ObjectData(player.getUniqueId());

        dataMap.put(player.getUniqueId(), data);
    }
}
