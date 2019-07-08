package cc.funkemunky.anticheat.data;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleDataManager implements DataManager {
    private Map<UUID, SimplePlayerData> dataMap = new HashMap<>();

    public void addData(Player player) {
        dataMap.put(player.getUniqueId(), new SimplePlayerData(player.getUniqueId()));
    }

    public void removeData(Player player) {
        dataMap.remove(player.getUniqueId());
    }

    public SimplePlayerData getPlayerData(UUID uuid) {
        return dataMap.getOrDefault(uuid, null);
    }
}
