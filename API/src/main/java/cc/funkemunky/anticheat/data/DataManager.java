package cc.funkemunky.anticheat.data;

import org.bukkit.entity.Player;

public interface DataManager {
    void addData(Player player);

    void removeData(Player player);
}
