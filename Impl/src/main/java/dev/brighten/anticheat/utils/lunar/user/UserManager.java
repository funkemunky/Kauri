package dev.brighten.anticheat.utils.lunar.user;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class UserManager {

    /* User Maps */
    private Map<UUID, String> playerNameCache;
    private Map<UUID, User> playerDataMap;

    public UserManager() {
        this.playerDataMap = new HashMap<>();
        this.playerNameCache = new HashMap<>();
    }

    /**
     * Get the player user object by uuid.
     *
     * @param uuid the uuid of the player.
     * @return the player user object.
     */
    public User getPlayerData(UUID uuid) {
        return this.playerDataMap.get(uuid);
    }

    /**
     * Get the player user object by player object.
     *
     * @param player the player object.
     * @return the player user object.
     */
    public User getPlayerData(Player player) {
        if (!this.playerNameCache.containsKey(player.getUniqueId())) {
            this.playerNameCache.put(player.getUniqueId(), player.getName());
        }
        return this.getPlayerData(player.getUniqueId());
    }

    /**
     * Set the player user object in the HashMap by the uuid.
     *
     * @param uuid the uuid of the player.
     * @param data the user object to set.
     */
    public void setPlayerData(UUID uuid, User data) {
        this.playerDataMap.put(uuid, data);
    }

    /**
     * Remove the player user object from the HashMap.
     *
     * @param uuid the uuid of the player to remove from the HashMap.
     */
    public void removePlayerData(UUID uuid) {
        this.playerDataMap.remove(uuid);
    }

}
