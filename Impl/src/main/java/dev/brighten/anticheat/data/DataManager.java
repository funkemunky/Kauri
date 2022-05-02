package dev.brighten.anticheat.data;

import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import dev.brighten.anticheat.check.api.Config;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DataManager {
    public final Int2ObjectMap<ObjectData> dataMap = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    public final IntOpenHashSet hasAlerts = new IntOpenHashSet(),
            devAlerts = new IntOpenHashSet();

    public DataManager() {
        /*
         * Running a timer every seconds to update the status of a player's ability to bypass detections.
         * This is done since permissions can be updated in real time so we want to make sure it's up to date
         * information without forcing the player to relog.
         */
        RunUtils.taskTimerAsync(() -> {
            dataMap.values().forEach(data -> data.bypassing = data.getPlayer().hasPermission("kauri.bypass")
                    && Config.flagBypassPerm);
        }, 60L, 20L);
    }

    /**
     * Grabs {@link dev.brighten.anticheat.data.ObjectData}
     * from {@link dev.brighten.anticheat.data.DataManager#dataMap} using param
     * player {@link java.util.UUID}'s hashcode.
     *
     * @param player {@link org.bukkit.entity.Player}
     * @return {@link dev.brighten.anticheat.data.ObjectData}
     */
    public ObjectData getData(Player player) {
        return dataMap.get(player.getUniqueId().hashCode());
    }

    /**
     * Removes a player's ObjectData from {@link dev.brighten.anticheat.data.DataManager#dataMap}
     *
     * @param player {@link org.bukkit.entity.Player}
     */
    public void removeData(Player player) {
        dataMap.remove(player.getUniqueId().hashCode());
    }

    /**
     * Initiates {@link dev.brighten.anticheat.data.ObjectData#ObjectData(UUID)} and inserts it into
     * {@link dev.brighten.anticheat.data.DataManager#dataMap}
     *
     * @param player {@link org.bukkit.entity.Player}
     */
    public void createData(Player player) {
        ObjectData data = new ObjectData(player.getUniqueId());

        dataMap.put(player.getUniqueId().hashCode(), data);
    }
}
