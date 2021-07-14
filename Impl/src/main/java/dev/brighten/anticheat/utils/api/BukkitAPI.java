package dev.brighten.anticheat.utils.api;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import dev.brighten.anticheat.utils.api.impl.LegacyAPI;
import dev.brighten.anticheat.utils.api.impl.NewAPI;
import org.bukkit.entity.Player;

public interface BukkitAPI {

    boolean isGliding(Player player);

    void setGliding(Player player, boolean state);

    static BukkitAPI INSTANCE = ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_9)
            ? new NewAPI() : new LegacyAPI();
}
