package dev.brighten.anticheat.utils.api.impl;

import dev.brighten.anticheat.utils.api.BukkitAPI;
import org.bukkit.entity.Player;

public class NewAPI implements BukkitAPI {
    @Override
    public boolean isGliding(Player player) {
        return player.isGliding();
    }

    @Override
    public void setGliding(Player player, boolean state) {

    }
}
