package dev.brighten.anticheat.commands.data;

import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import dev.brighten.anticheat.check.api.Config;
import org.bukkit.entity.Player;

public class DataManager {
    public final Int2ObjectMap<ObjectData> dataMap = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());
    public final IntOpenHashSet hasAlerts = new IntOpenHashSet(),
            devAlerts = new IntOpenHashSet();

    public DataManager() {
        RunUtils.taskTimerAsync(() -> {
            dataMap.values().forEach(data -> data.bypassing = data.getPlayer().hasPermission("kauri.bypass")
                    && Config.flagBypassPerm);
        }, 60L, 20L);
    }

    public ObjectData getData(Player player) {
        return dataMap.get(player.getUniqueId().hashCode());

    }

    public void createData(Player player) {
        ObjectData data = new ObjectData(player.getUniqueId());

        dataMap.put(player.getUniqueId().hashCode(), data);
    }
}
