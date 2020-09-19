package dev.brighten.anticheat.processing.keepalive;

import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingMap;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeepaliveProcessor implements Runnable {

    private BukkitTask task;

    public KeepAlive currentKeepalive;
    public int tick;

    public final Map<Short, KeepAlive> keepAlives = new ConcurrentEvictingMap<>(30);

    public ConcurrentHashMap<UUID, Short> lastResponses = new ConcurrentHashMap<>();

    public KeepaliveProcessor() {
        start();
    }

    @Override
    public void run() {
        tick++;
        synchronized (keepAlives) {
            currentKeepalive = new KeepAlive(tick);
            keepAlives.put(currentKeepalive.id, currentKeepalive);
        }

        WrappedOutTransaction packet = new WrappedOutTransaction(0, currentKeepalive.id, false);

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
        return keepAlives.values().parallelStream().filter(ka -> ka.start == tick).findFirst();
    }

    public Optional<KeepAlive> getKeepById(short id) {
        return Optional.ofNullable(keepAlives.get(id));
    }

    public Optional<KeepAlive> getResponse(ObjectData data) {
        if(!lastResponses.containsKey(data.uuid))
            return Optional.empty();

        return getKeepById(lastResponses.get(data.uuid));
    }

    public void start() {
        if(task == null) {
            task = RunUtils.taskTimer(this, 0L, 0L);
        }
    }

    public void addResponse(ObjectData data, short id) {
        getKeepById(id).ifPresent(ka -> {
            lastResponses.put(data.uuid, id);
            ka.received(data);
        });
    }

    public void stop() {
        if(task != null) {
            task.cancel();
            task = null;
        }
    }
}
