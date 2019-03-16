package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.utils.json.JSONException;
import cc.funkemunky.anticheat.api.utils.json.JSONObject;
import org.bukkit.entity.Player;

import java.io.IOException;

public class VPNUtils {

    public VPNResponse getResponse(Player player) {
        return getResponse(player.getAddress().getAddress().getHostAddress());
    }

    public VPNResponse getResponse(String ipAddress) {
        try {

            String url = "https://funkemunky.cc/vpn?license=FWd2EdCvm5CcSj9JFbiIWxkC73IFeDTlroLKrPvS&ip=" + ipAddress;

            JSONObject object = JsonReader.readJsonFromUrl(url);

            return VPNResponse.fromJson(object.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
