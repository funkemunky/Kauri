package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.utils.json.JSONException;
import cc.funkemunky.anticheat.api.utils.json.JSONObject;
import cc.funkemunky.anticheat.impl.config.CheckSettings;
import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;

public class VPNUtils {

    public VPNResponse getResponse(Player player) {
        return getResponse(player.getAddress().getAddress().getHostAddress());
    }

    public void cacheReponse(VPNResponse reponse) {

        try {
            Kauri.getInstance().getAntiPUPManager().vpnCache.inputField(reponse.getIp(), reponse.toJson().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public VPNResponse getIfCached(String ipAddress) {
        if( Kauri.getInstance().getAntiPUPManager().vpnCache.getDatabaseValues().containsKey(ipAddress)) {
            try {
                return VPNResponse.fromJson((String)  Kauri.getInstance().getAntiPUPManager().vpnCache.getField(ipAddress));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public VPNResponse getResponse(String ipAddress) {
        try {

            val response = getIfCached(ipAddress);

            if(response != null) return response;

            String license = !CheckSettings.override ? (Bukkit.getPluginManager().isPluginEnabled("KauriLoader") ? Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license") : "none") : CheckSettings.license;

            String url = "https://funkemunky.cc/vpn?license=" + license + "&ip=" + ipAddress;

            JSONObject object = JsonReader.readJsonFromUrl(url);

            if (!object.has("ip")) {
                return null;
            }

            val toCacheAndReturn = VPNResponse.fromJson(object.toString());

            cacheReponse(toCacheAndReturn);

            return toCacheAndReturn;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
