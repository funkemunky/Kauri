package dev.brighten.anticheat.utils.lunar.module.hologram;

import dev.brighten.anticheat.Kauri;
import lombok.Getter;
import lombok.Setter;
import dev.brighten.anticheat.utils.lunar.LunarClientAPI;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.brighten.anticheat.utils.lunar.util.BufferUtils.*;

@Getter
@Setter
public class Hologram {
    /* uuid of hologram */
    private UUID id;

    /* name of hologram */
    private String name;

    /* the world of the hologram */
    private World world;

    /* players with the hologram enabled */
    private List<UUID> enabled;

    /* location of hologram */
    private int x;
    private int y;
    private int z;

    /* lines of holograms */
    private List<String> lines;

    /**
     * create the object of a hologram.
     *
     * @param id the id of the hologram.
     * @param name the name of the hologram.
     * @param x the x location of a hologram.
     * @param y the y location of a hologram.
     * @param z the z location of a hologram.
     * @param world the world of the hologram.
     * @param lines the lines of the hologram.
     */
    public Hologram(UUID id, String name, int x, int y, int z, World world, String... lines){
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.lines = new ArrayList<>();
        this.enabled = new ArrayList<>();
        this.lines.addAll(Arrays.asList(lines));
    }

    /**
     * add a line to the array list.
     *
     * @param text the text of the line.
     * @param index the index of the line.
     */
    public void addLine(String text, int index){
        this.lines.add(index, text);
    }

    /**
     * remove a line from the array list.
     *
     * @param index the index of the line.
     */
    public void removeLine(int index){
        this.lines.remove(index);
    }

    /**
     * enable hologram for player
     *
     * @param player the player to enable it for.
     */
    public void enable(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        if (this.enabled.contains(player.getUniqueId())){
            this.update(player);
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(4);
        os.write(getBytesFromUUID(this.id));
        os.write(writeDouble(this.x));
        os.write(writeDouble(this.y));
        os.write(writeDouble(this.z));

        os.write(writeVarInt(this.lines.size()));
        for (String text : this.lines){
            os.write(writeString(text));
        }

        os.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", os.toByteArray());

        this.enabled.add(player.getUniqueId());
    }

    /**
     * update hologram for player.
     *
     * @param player the player to enable it for.
     */
    public void update(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        if (!this.enabled.contains(player.getUniqueId())){
            this.enable(player);
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(5);
        os.write(getBytesFromUUID(this.id));

        os.write(writeVarInt(this.lines.size()));
        for (String text : this.lines){
            os.write(writeString(text));
        }

        os.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", os.toByteArray());
    }

    /**
     * disable hologram for player.
     *
     * @param player the player to disable it for.
     */
    public void disable(Player player) throws IOException {
        if (!LunarClientAPI.getInstance().isAuthenticated(player)){
            return;
        }

        if (!this.enabled.contains(player.getUniqueId())){
            return;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(6);
        os.write(getBytesFromUUID(this.id));

        os.close();

        player.sendPluginMessage(Kauri.INSTANCE, "Lunar-Client", os.toByteArray());

        this.enabled.remove(player.getUniqueId());
    }
}
