package dev.brighten.anticheat.utils.lunar.module.hologram;

import dev.brighten.anticheat.Kauri;
import lombok.Getter;
import lombok.Setter;
import dev.brighten.anticheat.utils.lunar.listener.HologramListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class HologramManager {
    /* list of holograms */
    private List<Hologram> hologramList;

    /**
     * setup hologram manager.
     */
    public HologramManager(){
        this.hologramList = new ArrayList<>();
        Kauri.INSTANCE.getServer().getPluginManager().registerEvents(new HologramListener(), Kauri.INSTANCE);
    }

    /**
     * create a hologram.
     *
     * @param name the name of hologram.
     * @param location the location of hologram.
     */
    public void createHologram(String name, Location location, String... lines){
        if (this.getHologram(name) != null){
            return;
        }
        this.hologramList.add(new Hologram(UUID.randomUUID(), name, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld(), lines));
    }

    /**
     * delete a hologram.
     *
     * @param name the name of the hologram.
     */
    public void deleteHologram(String name) throws IOException {
        if (this.getHologram(name) == null){
            return;
        }

        Hologram hologram = this.getHologram(name);

        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            hologram.disable(player);
        }

        this.hologramList.remove(hologram);
    }

    /**
     * reload holograms for the player.
     *
     * @param player the player to reload holograms for.
     */
    public void reloadHolograms(Player player) throws IOException {
        for (Hologram hologram : this.hologramList){
            if (hologram.getWorld() == player.getLocation().getWorld()){
                hologram.enable(player);
            } else {
                hologram.disable(player);
            }
        }
    }

    /**
     * get hologram by name.
     *
     * @param name the name of the hologram.
     * @return the hologram with the name.
     */
    public Hologram getHologram(String name){
        for (Hologram hologram : this.hologramList){
            if (hologram.getName().equalsIgnoreCase(name)){
                return hologram;
            }
        }
        return null;
    }
}
