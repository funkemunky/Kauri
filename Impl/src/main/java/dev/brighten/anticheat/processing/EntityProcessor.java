package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityProcessor {

    public final Map<UUID, Set<Entity>> vehicles = Collections.synchronizedMap(new HashMap<>()),
            allEntitiesNearPlayer = Collections.synchronizedMap(new HashMap<>());
    public BukkitTask task, playerTask;

    private void runEntityProcessor() {
        synchronized (vehicles) {
            vehicles.clear();

            for (Map.Entry<UUID, Set<Entity>> entry : allEntitiesNearPlayer.entrySet()) {
                Set<Entity> vehicleSet = new HashSet<>();
                for (Entity entity : entry.getValue()) {
                    if(entity instanceof Vehicle)
                        vehicleSet.add(entity);
                }

                if(vehicleSet.size() > 0)
                    vehicles.put(entry.getKey(), vehicleSet);
            }
        }
    }

    private void runEntitiesNearPlayer() {
        allEntitiesNearPlayer.clear();
        for (ObjectData data : Kauri.INSTANCE.dataManager.dataMap.values()) {
            Set<Entity> entitiesNear = new HashSet<>();
            for (Entity value : Atlas.getInstance().getTrackedEntities().values()) {
                if(value.getLocation().distanceSquared(data.getPlayer().getLocation()) <= 25) {
                    entitiesNear.add(value);
                }
            }
            allEntitiesNearPlayer.put(data.getUUID(), entitiesNear);
        }
    }

    public static EntityProcessor start() {
        EntityProcessor processor = new EntityProcessor();
        processor.task = RunUtils.taskTimerAsync(processor::runEntityProcessor, Kauri.INSTANCE,
                0L, 20L);
        processor.playerTask = RunUtils.taskTimerAsync(processor::runEntitiesNearPlayer, Kauri.INSTANCE,
                0L, 20L);
        return processor;
    }
}
