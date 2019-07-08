package cc.funkemunky.anticheat.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SimplePlayerData extends PlayerData {
    public Player player;

    public SimplePlayerData(UUID uuid) {
        super(uuid);
        this.player = Bukkit.getPlayer(uuid);
    }
}
