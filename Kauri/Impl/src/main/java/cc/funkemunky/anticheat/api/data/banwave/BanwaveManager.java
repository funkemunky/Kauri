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

        executor.scheduleAtFixedRate(() -> {
            if (BanwaveConfig.getInstance().enabled) {
                runJudgementDay();
            }
        }, BanwaveConfig.getInstance().intervalTime, BanwaveConfig.getInstance().intervalTime, TimeUnit.valueOf(BanwaveConfig.getInstance().intervalUnit.toUpperCase()));
    }

    public void runJudgementDay() {
        service.execute(() -> {
            log("&cStarted banwave!");
            broadcast(BanwaveConfig.getInstance().startBanwave);
            log("&7Judging players...");
            val judged = judgeCheaters();
            log("&7Found a total of &e" + judged.size() + " cheaters&7.");

            if (!BanwaveConfig.getInstance().banInstantly) {
                AtomicInteger integer = new AtomicInteger(0);

                new BukkitRunnable() {
                    public void run() {
                        if (integer.get() < judged.size()) {
                            banCheater(judged.get(integer.get()));
                            integer.getAndIncrement();
                        } else {
                            log("&aCompleted!");
                            broadcast(BanwaveConfig.getInstance().completed);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Kauri.getInstance(), 0, BanwaveConfig.getInstance().banSeconds * 20);
            } else {
                judged.forEach(this::banCheater);

                log("&aCompleted!");
                broadcast(BanwaveConfig.getInstance().completed);
            }
        });
    }

    public void banCheater(UUID uuid) {
        val name = getPlayerName(uuid);
        broadcast(addPlayerName(BanwaveConfig.getInstance().foundCheater, name));

        new BukkitRunnable() {
            public void run() {
                Bukkit.dispatchCommand(Atlas.getInstance().getConsoleSender(), addPlayerName(BanwaveConfig.getInstance().punishCommand, name));
            }
        }.runTask(Kauri.getInstance());
    }

    public List<UUID> judgeCheaters() {
        List<UUID> uuids = new ArrayList<>();
        Kauri.getInstance().getLoggerManager().getViolations().keySet().stream().filter(key -> {
            val violationsToSort = Kauri.getInstance().getLoggerManager().getViolations(key);

            Map<Check, Integer> violations = new HashMap<>();

            violationsToSort.keySet().stream()
                    .filter(key2 -> Kauri.getInstance().getCheckManager().isCheck(key2) && Kauri.getInstance().getCheckManager().getCheck(key2).isBanWave())
                    .forEach(key2 -> violations.put(Kauri.getInstance().getCheckManager().getCheck(key2), violationsToSort.get(key2)));

            return violations.keySet().stream().anyMatch(key2 -> key2.getBanWaveThreshold() > violations.get(key2));
        }).forEach(uuids::add);

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
        MiscUtils.printToConsole(message);
        if (BanwaveConfig.getInstance().msgAdmins) {
            Kauri.getInstance().getDataManager().getDataObjects().keySet().stream()
                    .filter(key -> Kauri.getInstance().getDataManager().getDataObjects().get(key).isAlertsEnabled())
                    .forEach(key -> {
                        val data = Kauri.getInstance().getDataManager().getDataObjects().get(key);

                        data.getPlayer().sendMessage(Color.translate("&8[&6&lKauri&8] " + message));
                    });
        }
    }

    private void broadcast(String message) {
        if (BanwaveConfig.getInstance().broadcast) Bukkit.broadcastMessage(Color.translate(message));
    }
}
