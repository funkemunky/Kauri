package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.MathHelper;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadHandler {
    private static final List<UUID> players = new ArrayList<>();
    private static final LinkedList<ExecutorService> services = new LinkedList<>
            (Collections.singleton(Executors.newSingleThreadExecutor()));

    public static void addPlayer(Player player) {
        runExecutorCalcs();
        players.add(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        runExecutorCalcs();
        players.remove(player.getUniqueId());
    }

    public static ExecutorService getThread(UUID uuid) {
        int indexServices = MathHelper.floor_float(players.indexOf(uuid) / 20F);

        assert services.size() > 0 && services.size() < indexServices + 1
                : String.format("Index won't work for services (index=%s, size=%s)", indexServices, services.size());

        return services.get(indexServices);
    }

    private static int calculateRequiredThreadCount() {
        return MathHelper.ceiling_float_int(players.size() / 20F);
    }

    private static void runExecutorCalcs() {
        int required = calculateRequiredThreadCount();

        if(required > services.size()) {
            while(required > services.size()) {
                services.addLast(Executors.newSingleThreadExecutor());
            }
        } else if(required < services.size() && required > 0) {
            while(required < services.size()) {
                services.removeLast();
            }
        }
    }

    public static int threadCount() {
        return services.size();
    }

    public static void shutdown() {
        players.clear();
        for (ExecutorService service : services) {
            service.shutdownNow();
        }
        services.clear();
    }
}
