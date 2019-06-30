package cc.funkemunky.anticheat.api.data.banwave;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
