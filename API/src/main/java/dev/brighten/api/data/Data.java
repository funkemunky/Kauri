package dev.brighten.api.data;

import cc.funkemunky.api.handlers.ModData;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

public interface Data {

    void reloadChecks();

    void unloadChecks();

    void loadChecks();

    void unregister();

    UUID getUUID();

    Player getPlayer();

    @Deprecated
    ExecutorService getThread();

    boolean isUsingLunar();

    ProtocolVersion getClientVersion();

    ModData getForgeMods();
}
