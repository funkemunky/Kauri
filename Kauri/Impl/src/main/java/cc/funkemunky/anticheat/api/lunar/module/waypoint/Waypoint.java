package cc.funkemunky.anticheat.api.lunar.module.waypoint;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.anticheat.api.lunar.LunarClientAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static cc.funkemunky.anticheat.api.lunar.util.BufferUtils.*;

@Getter
@Setter
public class Waypoint {
    /* name & uuid of the waypoint */
    private String name;

    /* location of packet */
    private Location location;

    /* color information */
    private int red;
    private int green;
    private int blue;

    /* forced information */
    private boolean forced;

    /**
     * creates the object for the waypoint.
     * @param name the name of the waypoint.
     * @param red the red in the waypoint.
     * @param green the green in the waypoint.
     * @param blue the blue in the waypoint.
     * @param forced if the waypoint is forced or not.
     */
    public Waypoint(String name, Location location, int red, int green, int blue, boolean forced) {
        this.name = name;
        this.location = location;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.forced = forced;
    }

    /**
     * assign the waypoint to the player.
     * @param player the player instance.
     */
    public void enable(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write((byte) 23);
        os.write(writeString(this.name));
        os.write(writeString(this.location.getWorld().getUID().toString()));
        os.write(writeRGB(this.red, this.green, this.blue));
        os.write(writeInt(this.location.getBlockX()));
        os.write(writeInt(this.location.getBlockY()));
        os.write(writeInt(this.location.getBlockZ()));
        os.write(writeBoolean(forced));
        os.write(1);

        os.close();

        player.sendPluginMessage(Atlas.getInstance(), "Lunar-Client", os.toByteArray());
    }

    /**
     * disable the waypoint for the player.
     * @param player the player instance.
     */
    public void disable(Player player, boolean enable) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write((byte)24);
        os.write(writeString(this.name));
        os.write(writeString(this.location.getWorld().getUID().toString()));

        os.close();

        player.sendPluginMessage(Atlas.getInstance(), "Lunar-Client", os.toByteArray());

        if (enable) {
            this.enable(player);
        }
    }
}
