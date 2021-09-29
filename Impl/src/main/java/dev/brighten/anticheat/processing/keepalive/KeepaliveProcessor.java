package dev.brighten.anticheat.processing.keepalive;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.objects.evicting.ConcurrentEvictingMap;
import cc.funkemunky.api.utils.objects.evicting.EvictingMap;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class KeepaliveProcessor implements Runnable {

    private BukkitTask task;

    public KeepAlive currentKeepalive;
    public int tick;

    public final Map<Short, KeepAlive> keepAlives = new EvictingMap<>(80);

    public ConcurrentHashMap<UUID, Short> lastResponses = new ConcurrentHashMap<>();

    public KeepaliveProcessor() {
        start();
    }

    @Override
    public void run() {
        tick++;
        synchronized (keepAlives) {
            short id = (short) (tick > Short.MAX_VALUE ? tick % Short.MAX_VALUE : tick);

            //Ensuring we don't have any duplicate IDS

            currentKeepalive = new KeepAlive(tick, id);
            keepAlives.put(currentKeepalive.id, currentKeepalive);
        }

        WrappedOutTransaction packet = new WrappedOutTransaction(0, currentKeepalive.id, false);

        currentKeepalive.startStamp = System.currentTimeMillis();
        for (ObjectData value : Kauri.INSTANCE.dataManager.dataMap.valueCollection()) {
            if(value.target != null) {
                value.targetPastLocation.addLocation(value.target.getLocation());
                value.runKeepaliveAction(ka -> {
                    value.targetLoc = new KLocation(value.target.getLocation());
                });
            }

            double dh = value.playerInfo.deltaXZ, dy = Math.abs(value.playerInfo.deltaY);
            if(dh < 1 && dy < 1)
                value.playerInfo.nearbyEntities = value.getPlayer().getNearbyEntities(1 + dh, 2 + dy, 1 + dh);
            else value.playerInfo.nearbyEntities = Collections.emptyList();

            //Checking for AtlasBungee stuff incase a player joined before a heartbeat check could be sent
            //by Atlas
            if(Atlas.getInstance().getBungeeManager().isBungee()) {
                value.atlasBungeeInstalled = Atlas.getInstance().getBungeeManager().isAtlasBungeeInstalled();
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
        return keepAlives.values().stream().filter(ka -> ka.start == tick).findFirst();
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
