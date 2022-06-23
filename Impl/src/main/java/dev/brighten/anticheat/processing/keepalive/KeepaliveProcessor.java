package dev.brighten.anticheat.processing.keepalive;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import cc.funkemunky.api.utils.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import cc.funkemunky.api.utils.objects.evicting.EvictingMap;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class KeepaliveProcessor implements Runnable {

    private BukkitTask task;

    public KeepAlive currentKeepalive = new KeepAlive(0, (short)0);
    public long tick;
    public int totalPlayers, laggyPlayers;

    public final Cache<Integer, KeepAlive> keepAlives = CacheBuilder.newBuilder().concurrencyLevel(4)
            .expireAfterWrite(15, TimeUnit.SECONDS).build();

    final Int2ObjectMap<Integer> lastResponses = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<>());

    public KeepaliveProcessor() {
        start();
    }

    @Override
    public void run() {
        tick++;
        synchronized (keepAlives) {
            if(tick > Integer.MAX_VALUE - 1) {
                tick = 0;
            }

            int id = (int) tick;
            currentKeepalive = new KeepAlive(tick, id);
            keepAlives.put(currentKeepalive.id, currentKeepalive);
        }

        WrapperPlayServerPing packet = new WrapperPlayServerPing(currentKeepalive.id);

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
                if (dh < 1 && dy < 1)
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
        return keepAlives.asMap().values().stream().filter(ka -> ka.start == tick).findFirst();
    }

    public Optional<KeepAlive> getKeepById(int id) {
        return Optional.ofNullable(keepAlives.getIfPresent(id));
    }

    public Optional<KeepAlive> getResponse(ObjectData data) {
        if(!lastResponses.containsKey(data.uuid.hashCode()))
            return Optional.empty();

        return getKeepById(lastResponses.get(data.uuid.hashCode()));
    }

    public void start() {
        if(task == null) {
            task = RunUtils.taskTimer(this, Kauri.INSTANCE, 20L, 0L);
        }
    }

    public void addResponse(ObjectData data, int id) {
        getKeepById(id).ifPresent(ka -> {
            lastResponses.put(data.uuid.hashCode(), (Integer)id);
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
