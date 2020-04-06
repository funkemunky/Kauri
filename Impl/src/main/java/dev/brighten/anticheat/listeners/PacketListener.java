package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Init
public class PacketListener implements AtlasListener {

    public static ExecutorService packetThread = Executors.newFixedThreadPool(3);
    @Listen(ignoreCancelled = true, priority = ListenerPriority.LOW)
    public void onEvent(PacketReceiveEvent event) {
        if(event.getPlayer() == null) return;
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        packetThread.execute(() -> Kauri.INSTANCE.packetProcessor.processClient(event,
                data, event.getPacket(), event.getType(), event.getTimeStamp()));
    }

    @Listen(ignoreCancelled = true,priority = ListenerPriority.LOW)
    public void onEvent(PacketSendEvent event) {
        if(event.isCancelled() || event.getPlayer() == null
                || !Kauri.INSTANCE.enabled || event.getPacket() == null) return;

        if(!Kauri.INSTANCE.dataManager.dataMap.containsKey(event.getPlayer().getUniqueId()))
            return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        packetThread.execute(() -> Kauri.INSTANCE.packetProcessor.processServer(event,
                data, event.getPacket(), event.getType(), event.getTimeStamp()));
    }
}
