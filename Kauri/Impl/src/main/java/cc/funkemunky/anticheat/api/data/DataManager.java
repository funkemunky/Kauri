package cc.funkemunky.anticheat.api.data;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.CustomLocation;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DataManager {
    private Map<UUID, PlayerData> dataObjects = new ConcurrentHashMap<>();

    public DataManager() {
        new BukkitRunnable() {
            public void run() {
                dataObjects.keySet().parallelStream().forEach(key -> {
                    PlayerData data = dataObjects.get(key);
                    if (data.getTarget() != null && !data.getTarget().isDead()) {
                        data.setEntityFrom(data.getEntityTo());
                        data.setEntityTo(new CustomLocation(data.getTarget().getLocation()));
                        data.getEntityPastLocation().addLocation(data.getEntityTo());
                    }
                });
            }
        }.runTaskTimerAsynchronously(Kauri.getInstance(), 0L, 1L);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return dataObjects.getOrDefault(uuid, null);
    }

    public void addData(UUID uuid) {
        dataObjects.put(uuid, new PlayerData(uuid));
    }

    public void removeData(UUID uuid) {
        dataObjects.remove(uuid);
    }

    public void registerAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> addData(player.getUniqueId()));
    }
}
