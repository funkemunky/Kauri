package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityProcessor {

    public Map<UUID, List<Entity>> vehicles = new ConcurrentHashMap<>();
    public BukkitTask task;

    private void runEntityProcessor() {
        Atlas.getInstance().getEntities().keySet().forEach(uuid -> vehicles.put(uuid,
                Atlas.getInstance().getEntities().get(uuid)
                        .stream()
                        .filter(entity -> entity instanceof Vehicle)
                        .collect(Collectors.toList())));
    }

    public static EntityProcessor start() {
        EntityProcessor processor = new EntityProcessor();
        processor.task = RunUtils.taskTimerAsync(processor::runEntityProcessor, Kauri.INSTANCE, 0L, 10L);
        return processor;
    }
}
