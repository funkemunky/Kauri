package dev.brighten.anticheat.processing;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityProcessor {

    public static Map<UUID, List<Entity>> vehicles = new ConcurrentHashMap<>();
    public static BukkitTask task;

    private static void runEntityProcessor() {
        for (World world : Bukkit.getWorlds()) {
            vehicles.put(world.getUID(), world.getEntities().parallelStream().filter(entity -> entity instanceof Vehicle).collect(Collectors.toList()));
        }
    }

    public static void start() {
        task = RunUtils.taskTimer(EntityProcessor::runEntityProcessor, 0L, 15L);
    }
}
