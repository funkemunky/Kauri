package dev.brighten.anticheat.processing.keepalive;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutKeepAlivePacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.KLocation;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import cc.funkemunky.api.utils.objects.evicting.EvictingMap;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KeepaliveProcessor implements Runnable {

    private BukkitTask task;

    public KeepAlive currentKeepalive;
    public int tick;
    public int totalPlayers, laggyPlayers;

    public final Map<Short, KeepAlive> keepAlives = new EvictingMap<>(80);

    final Int2ObjectMap<Short> lastResponses = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

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
        totalPlayers = laggyPlayers = 0;
        for (ObjectData value : Kauri.INSTANCE.dataManager.dataMap.values()) {
            totalPlayers++;

            if(value.lagInfo.lastPingDrop.isNotPassed(2)
                    || System.currentTimeMillis() - value.lagInfo.lastClientTrans > 135L) laggyPlayers++;
            if(value.target != null) {
                value.targetPastLocation.addLocation(value.target.getLocation());
            }

            TinyProtocolHandler.sendPacket(value.getPlayer(), packet);

            double dh = value.playerInfo.deltaXZ, dy = Math.abs(value.playerInfo.deltaY);
            if(tick % 5 == 0) {
                if(dh < 1 && dy < 1)
                    value.playerInfo.nearbyEntities = value.getPlayer()
                            .getNearbyEntities(2 + dh, 3 + dy, 2 + dh);
                else value.playerInfo.nearbyEntities = Collections.emptyList();
            }

            //Checking for AtlasBungee stuff incase a player joined before a heartbeat check could be sent
            //by Atlas
            if(Atlas.getInstance().getBungeeManager().isBungee()) {
                value.atlasBungeeInstalled = Atlas.getInstance().getBungeeManager().isAtlasBungeeInstalled();
            }
        }
    }

    public Optional<KeepAlive> getKeepByTick(int tick) {
        return keepAlives.values().stream().filter(ka -> ka.start == tick).findFirst();
    }

    public Optional<KeepAlive> getKeepById(short id) {
        return Optional.ofNullable(keepAlives.get(id));
    }

    public Optional<KeepAlive> getResponse(ObjectData data) {
        if(!lastResponses.containsKey(data.uuid.hashCode()))
            return Optional.empty();

        return getKeepById(lastResponses.get(data.uuid.hashCode()));
    }

    public void start() {
        if(task == null) {
            task = RunUtils.taskTimer(this, Kauri.INSTANCE, 0L, 0L);
        }
    }

    public void addResponse(ObjectData data, short id) {
        getKeepById(id).ifPresent(ka -> {
            lastResponses.put(data.uuid.hashCode(), (Short)id);
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
