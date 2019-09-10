package dev.brighten.anticheat.data;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataManager {
    public Map<UUID, ObjectData> dataMap = new ConcurrentHashMap<>();
    public List<ObjectData> hasAlerts = new CopyOnWriteArrayList<>(), debugging = new CopyOnWriteArrayList<>();
    public List<BukkitTask> tasks = new ArrayList<>();

    public DataManager() {
        RunUtils.taskTimerAsync(() -> {
            hasAlerts.clear();
            dataMap.values().stream().filter(data -> data.alerts).forEach(hasAlerts::add);
            debugging.clear();
            dataMap.values().stream().filter(data -> data.debugging != null).forEach(debugging::add);
        }, Kauri.INSTANCE, 60L, 30L);

        RunUtils.taskTimer(() -> dataMap.values().stream()
                        .filter(data -> data.target != null)
                        .forEach(data -> data.targetPastLocation.addLocation(data.target.getLocation())),
                Kauri.INSTANCE,
                1L,
                1L);
    }

    public ObjectData getData(Player player) {
        if(!dataMap.containsKey(player.getUniqueId())) {
            ObjectData data = new ObjectData(player.getUniqueId());

            dataMap.put(player.getUniqueId(), data);
            return data;
        }
        return dataMap.get(player.getUniqueId());
    }
}
