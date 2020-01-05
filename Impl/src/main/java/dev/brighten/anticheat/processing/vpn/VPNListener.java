package dev.brighten.anticheat.processing.vpn;

import cc.funkemunky.api.utils.JsonMessage;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VPNListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkPlayer(event.getPlayer());
    }

    private void checkPlayer(Player player) {
        Kauri.INSTANCE.vpnHandler.vpnThread.execute(() -> {
            VPNResponse response = Kauri.INSTANCE.vpnHandler.getResponse(player);

            if(response.isProxy()) {
                if(VPNConfig.alert) {
                    String message = Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("vpn-alerts", "&8[&6&lKauri&8] &e%player% &7is using a VPN.");

                    Kauri.INSTANCE.dataManager.hasAlerts
                            .forEach(data -> {
                                JsonMessage json = new JsonMessage();
                                json.addText(message)
                                        .addHoverText("&7IP: &f" + (!VPNConfig.hideIP &&
                                                data.getPlayer().hasPermission("kauri.antivpn.ip")
                                                ? response.getIp() : "HIDDEN"),
                                                "&7ISP: &f" + response.getIsp())
                                        .setClickEvent(JsonMessage.ClickableType.RunCommand, VPNConfig.alertCommand);
                            });
                }
                if(VPNConfig.kick) {
                    RunUtils.task(() -> player.kickPlayer(Kauri.INSTANCE.msgHandler.getLanguage()
                            .msg("vpn-kick", "&7No VPNs are allowed.")));
                }
            }
        });
    }
}
