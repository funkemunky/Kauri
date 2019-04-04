package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.utils.json.JSONException;
import cc.funkemunky.anticheat.api.utils.json.JSONObject;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

@Init
public class VPNUtils {

    @ConfigSetting(path = "antivpn.license", name = "code")
    private String license = "";

    @ConfigSetting(path = "antivpn.license", name = "override")
    private boolean override = false;

    public VPNResponse getResponse(Player player) {
        return getResponse(player.getAddress().getAddress().getHostAddress());
    }

    public VPNResponse getResponse(String ipAddress) {
        try {

            String license = !override ? (Bukkit.getPluginManager().isPluginEnabled("KauriLoader") ? Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license") : "none") : this.license;

            String url = "https://funkemunky.cc/vpn?license=" + license + "&ip=" + ipAddress;

            JSONObject object = JsonReader.readJsonFromUrl(url);

            if(!object.has("ip")) {
                return null;
            }
            return VPNResponse.fromJson(object.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
