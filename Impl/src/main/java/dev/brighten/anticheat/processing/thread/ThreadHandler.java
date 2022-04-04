package dev.brighten.anticheat.processing.thread;

import cc.funkemunky.api.utils.*;
import cc.funkemunky.api.utils.com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Init
public class ThreadHandler {
    @Getter
    private final List<PlayerThread> services = new ArrayList<>();
    private final Map<UUID, Integer> threadCorrelations = new HashMap<>();
    private final int maxThreads;
    public static ThreadHandler INSTANCE;

    public ThreadHandler() {
        INSTANCE = this;
        maxThreads = Runtime.getRuntime().availableProcessors() * 2;
    }

    public PlayerThread getThread(ObjectData player) {
        synchronized (threadCorrelations) {
            if(threadCorrelations.containsKey(player.getUUID())) {
                return services.get(threadCorrelations.get(player.getUUID()));
            }

            PlayerThread thread = services.size() < Math.max(1, maxThreads)
                    ?  generatePlayerThread()
                    : services.stream()
                    .min(Comparator.comparing(PlayerThread::getCount)).orElse(MiscUtils.randomElement(services));

            int index = services.indexOf(thread);

            thread.addCount();

            threadCorrelations.put(player.getUUID(), index);

            return thread;
        }
    }

    public void removePlayer(Player player) {
        synchronized (threadCorrelations) {
            if(threadCorrelations.containsKey(player.getUniqueId())) {
                int index = threadCorrelations.remove(player.getUniqueId());

                services.get(index).subtractCount();
            }
        }
    }

    private PlayerThread generatePlayerThread() {
        PlayerThread thread = new PlayerThread(Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("Kauri Player Thread " + services.size() + 1)
                .setUncaughtExceptionHandler((t, e) -> RunUtils.task(e::printStackTrace, Kauri.INSTANCE))
                .build()));

        services.add(thread);

        return thread;
    }

    public int threadCount() {
        return services.size();
    }

    public void shutdown() {
        threadCorrelations.clear();
        for (PlayerThread service : services) {
            service.getThread().shutdownNow();
        }
        services.clear();
    }
}
