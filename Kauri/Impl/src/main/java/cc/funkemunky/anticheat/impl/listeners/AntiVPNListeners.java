package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.VPNResponse;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;
import java.util.List;

@Init
public class AntiVPNListeners implements Listener {

    @ConfigSetting(path = "antivpn", name = "blockedCountries")
    private List<String> blockCountries = new ArrayList<>();

    @ConfigSetting(path = "antivpn", name = "kickReason.usingProxy")
    private String usingProxy = "&cYou are not allowed to use a VPN or Proxy.";

    @ConfigSetting(path = "antivpn", name = "kickReason.blockedCountry")
    private String blockedCountry = "&cThe country %countryName% is blocked from this server";

    @ConfigSetting(path = "antivpn", name = "kickReason.enabled")
    private boolean enabled = true;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerLoginEvent event) {
        if(enabled && !event.getPlayer().hasPermission("kauri.antivpn.bypass")) {
            Kauri.getInstance().getAntiPUPManager().pupThread.execute(() -> {
                long timestamp = System.currentTimeMillis();
                if(timestamp - Kauri.getInstance().lastLogin > 100L && event.getAddress() != null) {
                    VPNResponse response = Kauri.getInstance().getVpnUtils().getResponse(event.getAddress().getHostAddress());

                    if (response == null) return;

                    if (response.isProxy()) {
                        Bukkit.getScheduler().runTask(Kauri.getInstance(), () -> event.getPlayer().kickPlayer(Color.translate(usingProxy)));
                    } else if (blockCountries.contains(response.getCountryCode())) {
                        Bukkit.getScheduler().runTask(Kauri.getInstance(), () -> event.getPlayer().kickPlayer(Color.translate(blockedCountry.replaceAll("%countryName%", response.getCountryName()))));
                    }
                }
                Kauri.getInstance().lastLogin = timestamp;
            });
        }
    }
}
