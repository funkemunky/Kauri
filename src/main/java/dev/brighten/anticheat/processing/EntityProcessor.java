package dev.brighten.anticheat.processing;

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
        Map<UUID, List<LivingEntity>> entities = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            vehicles.put(world.getUID(), world.getEntities()
                    .parallelStream()
                    .filter(entity -> entity instanceof Vehicle)
                    .collect(Collectors.toList()));

            entities.put(world.getUID(), world.getEntities()
                    .parallelStream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .map(entity -> (LivingEntity)entity)
                    .collect(Collectors.toList()));
        }
        for (ObjectData data : Kauri.INSTANCE.dataManager.dataMap.values()) {
            List<LivingEntity> entityList = entities.get(data.getPlayer().getWorld().getUID());
            data.entitiesNearPlayer.clear();
            entityList
                    .parallelStream()
                    .filter(entity -> entity.getLocation().distance(data.getPlayer().getLocation()) < 6)
                    .sequential()
                    .forEach(data.entitiesNearPlayer::add);
        }
        entities.clear();
        entities = null;
    }

    public static void start() {
        task = RunUtils.taskTimerAsync(EntityProcessor::runEntityProcessor, Kauri.INSTANCE, 2L, 20L);
    }
}
