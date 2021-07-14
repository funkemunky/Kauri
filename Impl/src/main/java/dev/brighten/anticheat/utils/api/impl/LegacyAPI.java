package dev.brighten.anticheat.utils.api.impl;

import dev.brighten.anticheat.utils.api.BukkitAPI;
import org.bukkit.entity.Player;

public class LegacyAPI implements BukkitAPI {
    @Override
    public boolean isGliding(Player player) {
        return false;
    }

    @Override
    public void setGliding(Player player, boolean state) {
        player.setGliding(state);
    }
}
