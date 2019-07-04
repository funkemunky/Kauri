package cc.funkemunky.anticheat.api.data.banwave;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BanwaveManager {

    private ExecutorService service;
    private ScheduledExecutorService executor;

    public BanwaveManager() {
        executor = Executors.newSingleThreadScheduledExecutor();
        service = Executors.newSingleThreadExecutor();
    }

    public void runJudgementDay() {

    }

    public void banCheater(UUID uuid) {

    }

    public List<UUID> judgeCheaters() {
        List<UUID> uuids = new ArrayList<>();

        return uuids;
    }

    private String getPlayerName(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    private String addPlayerName(String string, UUID uuid) {
        return addPlayerName(string, getPlayerName(uuid));
    }

    private String addPlayerName(String string, String name) {
        return string.replace("%player%", name);
    }

    private void log(String message) {

    }

    private void broadcast(String message) {

    }
}
