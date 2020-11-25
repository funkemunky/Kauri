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
        Atlas.getInstance().getEntities().keySet().parallelStream()
                .map(uuid -> Atlas.getInstance().getEntities().get(uuid))
                .filter(entity -> entity instanceof Vehicle)
                .sequential()
                .forEach(entity -> {
                    vehicles.compute(entity.getWorld().getUID(), (key, entities) -> {
                        if(entities == null) entities = new CopyOnWriteArrayList<>();

                        entities.add(entity);

                        return entities;
                    });
                });
    }

    private void runEntitiesNearPlayer() {
        Bukkit.getOnlinePlayers().parallelStream()
                .map(p -> new Tuple<Player, List<Entity>>(p, p.getNearbyEntities(5, 5, 5)))
                .sequential()
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
