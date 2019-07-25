package cc.funkemunky.anticheat.api.lunar.module.waypoint;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import cc.funkemunky.anticheat.api.lunar.listener.WaypointListener;
import cc.funkemunky.anticheat.api.lunar.module.waypoint.Waypoint;
import cc.funkemunky.anticheat.api.lunar.util.type.Minimap;
import cc.funkemunky.anticheat.api.lunar.util.type.ServerRule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static cc.funkemunky.anticheat.api.lunar.util.BufferUtils.writeString;

@Getter
@Setter
public class WaypointManager {
    /* the list of waypoints */
    private List<Waypoint> waypoints;

    /* waypoints */
    private HashMap<UUID, List<Waypoint>> personalWaypoints;

    /**
     * the manager of waypoints.
     */
    public WaypointManager() {
        waypoints = new ArrayList<>();
        personalWaypoints = new HashMap<>();
        WaypointListener listener = new WaypointListener();
        Atlas.getInstance().getServer().getPluginManager().registerEvents(listener, Atlas.getInstance());
        Atlas.getInstance().getEventManager().registerListeners(listener, Atlas.getInstance());
    }

    /**
     * create the waypoint instance.
     * @param name the name of the waypoint.
     * @param location the location of the waypoint.
     * @param red the red color of the waypoint.
     * @param green the green color of the waypoint.
     * @param blue the blue color of the waypoint.
     * @param forced if the waypoint is forced.
     */
    public void createWaypoint(String name, Location location, int red, int green, int blue, boolean forced) {
        this.waypoints.add(new Waypoint(name, location, red, green, blue, forced));
    }

    /**
     * create the personal waypoint instance.
     * @param name the name of the waypoint.
     * @param player the player uuid.
     * @param location the location of the waypoint.
     * @param red the red color of the waypoint.
     * @param green the green color of the waypoint.
     * @param blue the blue color of the waypoint.
     * @param forced if the waypoint is forced.
     */
    public void createWaypoint(String name, UUID player, Location location, int red, int green, int blue, boolean forced) {
        List<Waypoint> waypoints = new ArrayList<>();

        if (personalWaypoints.containsKey(player)) {
            waypoints = personalWaypoints.get(player);
        }

        waypoints.add(new Waypoint(name, location, red, green, blue, forced));

        personalWaypoints.put(player, waypoints);
    }

    /**
     * delete the waypoint instance.
     * @param name the name of the waypoint you want to delete.
     */
    public void deleteWaypoint(String name, UUID player) {
        Waypoint waypoint = getWaypoint(name, player);

        if (waypoint == null) {
            return;
        }

        for (Player target : Bukkit.getServer().getOnlinePlayers()) {
            try {
                waypoint.disable(target, false);
            } catch (IOException e) {
                //ignore
            }
        }

        if (personalWaypoints.containsKey(player)) {
            List<Waypoint> waypoints = personalWaypoints.get(player);

            waypoints.remove(waypoint);

            personalWaypoints.put(player, waypoints);
        }

        this.waypoints.remove(waypoint);
    }

    /**
     * reload the waypoints for the player.
     * @param player the player to reload the waypoints for.
     */
    public void reloadWaypoints(Player player, boolean enable) {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)) {
            return;
        }

        if (personalWaypoints.containsKey(player.getUniqueId())) {
            for (Waypoint waypoint : personalWaypoints.get(player.getUniqueId())) {
                try {
                    waypoint.disable(player, enable);
                } catch (IOException e) {
                    //ignore
                }
            }
        }

        for (Waypoint waypoint : this.waypoints) {
            try {
                waypoint.disable(player, enable);
            } catch (IOException e) {
                //ignore
            }
        }

        try {
            ByteArrayOutputStream packetFifteen = new ByteArrayOutputStream();

            packetFifteen.write((byte) 15);
            packetFifteen.write(writeString(player.getLocation().getWorld().getUID().toString()));

            packetFifteen.close();

            player.sendPluginMessage(Atlas.getInstance(), "Lunar-Client", packetFifteen.toByteArray());
        } catch (IOException e) {
            //ignore
        }
    }

    /**
     * enable the minimap instance for the player.
     * @param player the player who would like to enable minimap.
     * @throws IOException if the packets fuck up.
     */
    public void setMiniMap(Player player, Minimap minimap) throws IOException {
        LunarClientAPI.getInstance().updateServerRule(player, ServerRule.MINIMAP_STATUS, false, 0, 0F, minimap.name());
    }

    /**
     * get the waypoint instance by the name.
     * @param name the name of the waypoint.
     * @return the waypoint with the name.
     */
    public Waypoint getWaypoint(String name, UUID player) {
        if (this.personalWaypoints.containsKey(player)) {
            for (Waypoint waypoint : this.personalWaypoints.get(player)) {
                if (waypoint.getName().equalsIgnoreCase(name)) {
                    return waypoint;
                }
            }
        }

        for (Waypoint waypoint : this.waypoints) {
            if (waypoint.getName().equalsIgnoreCase(name)) {
                return waypoint;
            }
        }
        return null;
    }
}
