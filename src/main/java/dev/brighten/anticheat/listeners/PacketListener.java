package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;

@Init
public class PacketListener implements AtlasListener {

    @Listen
    public void onEvent(PacketReceiveEvent event) {
        if(!Kauri.INSTANCE.enabled) return;

        if(!Kauri.INSTANCE.dataManager.dataMap.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());
        data.predictionService.onReceive(event);
        Kauri.INSTANCE.packetProcessor.processClient(data, event.getPacket(), event.getType(), event.getTimeStamp());
    }

    @Listen
    public void onEvent(PacketSendEvent event) {
        if(!Kauri.INSTANCE.enabled) return;

        if(!Kauri.INSTANCE.dataManager.dataMap.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());
        data.predictionService.onSend(event);
        Kauri.INSTANCE.packetProcessor.processServer(data, event.getPacket(), event.getType(), event.getTimeStamp());
    }
}
