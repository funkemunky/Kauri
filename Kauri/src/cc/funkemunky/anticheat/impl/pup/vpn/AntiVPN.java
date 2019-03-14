package cc.funkemunky.anticheat.impl.pup.vpn;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.pup.AntiPUP;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.api.utils.VPNResponse;
import cc.funkemunky.api.Atlas;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class AntiVPN extends AntiPUP {

    @Setting(name = "blockedCountries")
    private List<String> blockCountries = new ArrayList<>();

    public AntiVPN(String name, boolean enabled) {
        super(name, enabled);
    }

    @Override
    public boolean onPacket(Object packet, String packetType, long timestamp) {
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Atlas.getInstance().getThreadPool().execute(() -> {
            VPNResponse response = MiscUtils.getResponse(event.getPlayer());

            if(response.isUsingProxy()) {
                //TODO kick player for reason
            } else if(blockCountries.contains(response.getCountryCode())) {
                //TODO kick player for reason
            }
        });
    }
}
