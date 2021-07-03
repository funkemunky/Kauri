package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.Tuple;
import dev.brighten.anticheat.Kauri;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityProcessor {

    public Map<UUID, List<Entity>> vehicles = new ConcurrentHashMap<>(),
            allEntitiesNearPlayer = new ConcurrentHashMap<>();
    public BukkitTask task, playerTask;

    private void runEntityProcessor() {
        for (Map.Entry<UUID, Entity> entry
                : Atlas.getInstance().getEntities().entrySet()) {
            if(entry.getValue() instanceof Vehicle) {
                vehicles.compute(entry.getValue().getWorld().getUID(), (key, entities) -> {
                    if(entities == null) entities = new CopyOnWriteArrayList<>();

                    entities.add(entry.getValue());

                    return entities;
                });
            }
        }
    }

    private void runEntitiesNearPlayer() {
        Bukkit.getOnlinePlayers().stream()
                .map(p -> new Tuple<Player, List<Entity>>(p, p.getNearbyEntities(5, 5, 5)))
                .forEach(tuple -> allEntitiesNearPlayer.put(tuple.one.getUniqueId(), new ArrayList<>(tuple.two)));
    }

    public static EntityProcessor start() {
        EntityProcessor processor = new EntityProcessor();
        processor.task = RunUtils.taskTimerAsync(processor::runEntityProcessor, Kauri.INSTANCE,
                0L, 10L);
        processor.playerTask = RunUtils.taskTimer(processor::runEntitiesNearPlayer, Kauri.INSTANCE,
                0L, 20L);
        return processor;
    }
}
