package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@Init
public class PlayerConnectionListeners implements Listener {

    @ConfigSetting(path = "data.logging", name = "removeBanInfoOnJoin")
    private boolean removeBanOnJoin = true;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Atlas.getInstance().getThreadPool().execute(() -> {
            Kauri.getInstance().getDataManager().addData(event.getPlayer().getUniqueId());
            if (removeBanOnJoin && Kauri.getInstance().getLoggerManager().isBanned(event.getPlayer().getUniqueId())) {
                Kauri.getInstance().getLoggerManager().removeBan(event.getPlayer().getUniqueId());
            }
        });

        Atlas.getInstance().getSchedular().schedule(() -> {
            PlayerData data = Kauri.getInstance().getDataManager().getPlayerData(event.getPlayer().getUniqueId());
            WrappedOutPositionPacket pos = new WrappedOutPositionPacket();
            val loc = event.getPlayer().getLocation().clone();
            loc.setX(loc.getX() + 0.001);
            loc.setZ(loc.getZ() - 0.001);

            data.setTeleportLoc(loc);
            data.setTeleportTest(System.currentTimeMillis());
            pos.setPacket("PacketPlayOutPosition", new Object[] {loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), new HashSet<>()});

            TinyProtocolHandler.sendPacket(event.getPlayer(), pos.getObject());
        }, 2, TimeUnit.SECONDS);

        if(event.getPlayer().getName().equals("funkemunky")) {
            event.getPlayer().sendMessage(Color.Gray + "This server is using Kauri " + Kauri.getInstance().getDescription().getVersion());
        }

        if (ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9))
            event.getPlayer().removeAchievement(Achievement.OPEN_INVENTORY);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Kauri.getInstance().getDataManager().removeData(event.getPlayer().getUniqueId());
    }
}
