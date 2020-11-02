package dev.brighten.anticheat.listeners;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.ListenerPriority;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.GameMode;

@Init
public class AtlasCheckListeners implements AtlasListener {

    @Listen(priority = ListenerPriority.HIGHEST)
    public void onPacket(PacketReceiveEvent event) {
        if(event.getPlayer() == null) return;

        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(event.getType().equals(Packet.Client.CREATIVE_SLOT)
                && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }

    @Listen(priority = ListenerPriority.HIGHEST)
    public void onPacket(PacketSendEvent event) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(event.getPlayer());

        if(data != null) {
            data.checkManager.runEvent(event);
        }
    }
}
