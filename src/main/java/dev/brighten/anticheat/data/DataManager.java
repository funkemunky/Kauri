package dev.brighten.anticheat.data;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DataManager {
    public Map<Player, ObjectData> dataMap = new WeakHashMap<>();
    public List<ObjectData> hasAlerts = new CopyOnWriteArrayList<>();

    public DataManager() {
        RunUtils.taskTimerAsync(() -> {
            hasAlerts.clear();
            dataMap.values().stream().filter(data -> data.alerts).forEach(hasAlerts::add);
        }, Kauri.INSTANCE, 60L, 30L);
    }

    public ObjectData getData(Player player) {
        if(!dataMap.containsKey(player)) {
            ObjectData data = new ObjectData(player.getUniqueId());

            dataMap.put(player, data);
            return data;
        }
        return dataMap.get(player);
    }
}
