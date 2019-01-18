package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Kauri.getInstance().getDataManager().addData(event.getPlayer().getUniqueId());

        TinyProtocolHandler.sendPacket(event.getPlayer(), new WrappedOutTransaction(0, (short) 69, false).getObject());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Kauri.getInstance().getDataManager().removeData(event.getPlayer().getUniqueId());
    }
}
