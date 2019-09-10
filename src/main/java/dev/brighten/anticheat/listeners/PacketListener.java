package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;

@Init
public class PacketListener implements AtlasListener {

    @Listen
    public void onEvent(PacketReceiveEvent event) {
        if(!Kauri.INSTANCE.enabled) return;

        if(!Kauri.INSTANCE.dataManager.dataMap.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        Kauri.INSTANCE.packetProcessor.processClient(Kauri.INSTANCE.dataManager.getData(event.getPlayer()), event.getPacket(), event.getType());
    }

    @Listen
    public void onEvent(PacketSendEvent event) {
        if(!Kauri.INSTANCE.enabled) return;

        if(!Kauri.INSTANCE.dataManager.dataMap.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        Kauri.INSTANCE.packetProcessor.processServer(Kauri.INSTANCE.dataManager.getData(event.getPlayer()), event.getPacket(), event.getType());
    }
}
