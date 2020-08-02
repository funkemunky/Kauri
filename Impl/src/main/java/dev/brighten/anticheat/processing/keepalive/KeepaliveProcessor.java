package dev.brighten.anticheat.processing.keepalive;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.objects.evicting.EvictingList;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.*;

public class KeepaliveProcessor implements Runnable {

    private ScheduledFuture<?> task;

    public KeepAlive currentKeepalive;
    public int tick;

    public final List<KeepAlive> keepAlives = Collections.synchronizedList(new EvictingList<>(20));

    public ConcurrentHashMap<UUID, Integer> lastResponses = new ConcurrentHashMap<>();
    public ScheduledExecutorService executor;

    public KeepaliveProcessor() {
        executor = Executors.newSingleThreadScheduledExecutor();
        start();
    }

    @Override
    public void run() {
        tick++;
        synchronized (keepAlives) {
            keepAlives.add(currentKeepalive = new KeepAlive(tick));
        }

        WrappedOutTransaction packet = new WrappedOutTransaction((int)0, (short)currentKeepalive.id, false);

        currentKeepalive.startStamp = System.currentTimeMillis();
        for (ObjectData value : Kauri.INSTANCE.dataManager.dataMap.values()) {
            if(value.target != null) {
                value.targetPastLocation.addLocation(value.target.getLocation());
                value.runKeepaliveAction(ka -> {
                    value.targetLoc = new KLocation(value.target.getLocation());
                });
            }
            TinyProtocolHandler.sendPacket(value.getPlayer(), packet);
            /*value.getThread().execute(() -> {
                for (Runnable runnable : value.tasksToRun) {
                    runnable.run();
                    value.tasksToRun.remove(runnable);
                }
            });*/
        }
    }

    public Optional<KeepAlive> getKeepByTick(int tick) {
        return keepAlives.parallelStream().filter(ka -> ka.start == tick).findFirst();
    }

    public Optional<KeepAlive> getKeepById(int id) {
        return keepAlives.parallelStream().filter(ka -> ka.id == id).findFirst();
    }

    public Optional<KeepAlive> getResponse(ObjectData data) {
        if(!lastResponses.containsKey(data.uuid))
            return Optional.empty();

        return getKeepById(lastResponses.get(data.uuid));
    }

    public void start() {
        if(task == null) {
            task = executor.scheduleAtFixedRate(this, 50L, 50L, TimeUnit.MILLISECONDS);
        }
    }

    public void addResponse(ObjectData data, int id) {
        getKeepById(id).ifPresent(ka -> {
            lastResponses.put(data.uuid, id);
            ka.received(data);
        });
    }

    public void stop() {
        if(task != null) {
            task.cancel(true);
            executor.shutdown();
            task = null;
        }
    }
}
