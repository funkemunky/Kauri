package dev.brighten.api;

import cc.funkemunky.api.reflections.Reflections;
import dev.brighten.api.handlers.ExemptHandler;
import dev.brighten.api.wrappers.WrappedDataManager;
import dev.brighten.api.wrappers.WrappedKauri;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class KauriAPI {

    public static KauriAPI INSTANCE;

    public ExemptHandler exemptHandler;
    public ScheduledExecutorService service;
    public WrappedDataManager dataManager;
    private WrappedKauri kauriPlugin;

    public KauriAPI() {
        INSTANCE = this;
        exemptHandler = new ExemptHandler();
        service = Executors.newSingleThreadScheduledExecutor();
        kauriPlugin = new WrappedKauri(Reflections.getClass("dev.brighten.anticheat.Kauri")
                .getFieldByName("INSTANCE").get(null));
        dataManager = kauriPlugin.getDataManager();
    }

    public void reloadChecksForPlayer(Player player) {
        dataManager.getData(player).reloadChecks();
    }
}
