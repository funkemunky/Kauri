package dev.brighten.anticheat.utils.lunar.module.border;

import dev.brighten.anticheat.Kauri;
import lombok.Getter;
import lombok.Setter;
import dev.brighten.anticheat.utils.lunar.LunarClientAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static dev.brighten.anticheat.utils.lunar.util.BufferUtils.*;

@Getter
@Setter
public class Border {
    /* name of border */
    private String name;

    /* world of border */
    private World world;

    /* settings of border */
    private boolean cancelsExit;
    private boolean canShrinkExpand;

    /* color of border */
    private int red;
    private int green;
    private int blue;

    /* location of corners */
    private Location minLocation;
    private Location maxLocation;

    /**
     * create the object for a border.
     *
     * @param name the name of the border.
     * @param world the world of the border.
     * @param cancelsExit a setting for the border.
     * @param canShrinkExpand a setting for the border.
     * @param red the red color of the border.
     * @param green the green color of the border.
     * @param blue the blue color of the border.
     * @param minLocation the minimum location of the border.
     * @param maxLocation the maximum location of the border.
     */
    public Border(String name, World world, boolean cancelsExit, boolean canShrinkExpand, int red, int green, int blue, Location minLocation, Location maxLocation){
        this.name = name;
        this.world = world;
        this.cancelsExit = cancelsExit;
        this.canShrinkExpand = canShrinkExpand;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.minLocation = minLocation;
        this.maxLocation = maxLocation;
    }

    /**
     * enable world border for player.
     *
     * @param player the player to enable the border for.
     */
    public void enable(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(20);
        os.write(writeBoolean(true));
        os.write(writeString(this.name));
        os.write(writeString(this.world.getUID().toString()));
        os.write(writeBoolean(this.cancelsExit));
        os.write(writeBoolean(this.canShrinkExpand));
        os.write(writeRGB(this.red, this.green, this.blue));
        os.write(writeDouble(this.minLocation.getBlockX()));
        os.write(writeDouble(this.minLocation.getBlockZ()));
        os.write(writeDouble(this.maxLocation.getBlockX()));
        os.write(writeDouble(this.maxLocation.getBlockZ()));

        os.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", os.toByteArray());

        ByteArrayOutputStream packetFifteen = new ByteArrayOutputStream();

        packetFifteen.write((byte) 15);
        packetFifteen.write(writeString(this.world.getUID().toString()));

        packetFifteen.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", packetFifteen.toByteArray());
    }

    /**
     * update world border for player.
     *
     * @param player the player to update the border for.
     */
    public void update(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(22);
        os.write(writeString(this.name));
        os.write(writeDouble(this.minLocation.getBlockX()));
        os.write(writeDouble(this.minLocation.getBlockZ()));
        os.write(writeDouble(this.maxLocation.getBlockX()));
        os.write(writeDouble(this.maxLocation.getBlockZ()));

        os.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", os.toByteArray());

        ByteArrayOutputStream packetFifteen = new ByteArrayOutputStream();

        packetFifteen.write((byte) 15);
        packetFifteen.write(writeString(this.world.getUID().toString()));

        packetFifteen.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", packetFifteen.toByteArray());
    }

    /**
     * disable world border for player.
     *
     * @param player the player to disable the border for.
     */
    public void disable(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(21);
        os.write(writeString(this.name));

        os.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", os.toByteArray());

        ByteArrayOutputStream packetFifteen = new ByteArrayOutputStream();

        packetFifteen.write((byte) 15);
        packetFifteen.write(writeString(this.world.getUID().toString()));

        packetFifteen.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", packetFifteen.toByteArray());
    }
}
