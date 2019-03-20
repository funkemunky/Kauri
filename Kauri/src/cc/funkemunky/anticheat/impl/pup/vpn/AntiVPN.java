package cc.funkemunky.anticheat.impl.pup.vpn;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.pup.PuPType;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.VPNResponse;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.Color;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AntiVPN extends AntiPUP {

    @Setting(name = "blockedCountries")
    private List<String> blockCountries = new ArrayList<>();

    @Setting(name = "kickReason.usingProxy")
    private String usingProxy = "&cYou are not allowed to use a VPN or Proxy.";

    @Setting(name = "kickReason.blockedCountry")
    private String blockedCountry = "&cThe country %countryName% is blocked from this server";

    public AntiVPN(String name, PuPType type, boolean enabled) {
        super(name, type, enabled);
    }

    @Override
    public boolean onPacket(Object packet, String packetType, long timestamp) {
        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Atlas.getInstance().getThreadPool().execute(() -> {
            VPNResponse response = Kauri.getInstance().getVpnUtils().getResponse(event.getPlayer());

            if (response.isProxy()) {
                new BukkitRunnable() {
                    public void run() {
                        event.getPlayer().kickPlayer(Color.translate(usingProxy));
                    }
                }.runTask(Kauri.getInstance());
            } else if (blockCountries.contains(response.getCountryCode())) {
                new BukkitRunnable() {
                    public void run() {
                        event.getPlayer().kickPlayer(Color.translate(blockedCountry.replaceAll("%countryName%", response.getCountryName())));
                    }
                }.runTask(Kauri.getInstance());
            }
        });
    }
}
