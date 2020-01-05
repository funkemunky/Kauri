package dev.brighten.anticheat.processing.vpn;

import cc.funkemunky.carbon.utils.json.JSONException;
import cc.funkemunky.carbon.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VPNResponse {
    private String ip, countryName, countryCode, state, city, isp;
    private boolean success, proxy;

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("success", success);
        json.put("ip", ip);
        json.put("countryName", countryName);
        json.put("countryCode", countryCode);
        json.put("state", state);
        json.put("city", city);
        json.put("isp", isp);
        json.put("proxy", proxy);

        return json;
    }

    public static VPNResponse fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        return new VPNResponse(jsonObject.getString("ip"), jsonObject.getString("countryName"), jsonObject.getString("countryCode"), jsonObject.getString("state"), jsonObject.getString("city"), jsonObject.getString("isp"), jsonObject.getBoolean("success"), jsonObject.getBoolean("proxy"));
    }

}