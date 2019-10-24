package dev.brighten.anticheat.processing;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityProcessor {

    public static Map<UUID, List<Entity>> vehicles = new ConcurrentHashMap<>();
    public static BukkitTask task;

    private static void runEntityProcessor() {
        for (UUID uuid : Atlas.getInstance().getEntities().keySet()) {
            vehicles.put(uuid,
                    Atlas.getInstance().getEntities().get(uuid)
                            .stream()
                            .filter(entity -> entity instanceof Vehicle)
                            .collect(Collectors.toList()));
        }
    }

    public static void start() {
        task = RunUtils.taskTimerAsync(EntityProcessor::runEntityProcessor, Kauri.INSTANCE, 2L, 20L);
    }
}
