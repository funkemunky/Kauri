package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.api.utils.json.JSONException;
import cc.funkemunky.anticheat.api.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VPNResponse {
    private String ip, country, state, city, isp;
    private boolean proxy;

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("ip", ip);
        json.put("country", country);
        json.put("state", state);
        json.put("city", city);
        json.put("isp", isp);
        json.put("proxy", proxy);

        return json;
    }

    public static VPNResponse fromJson(String json)throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        return new VPNResponse(jsonObject.getString("ip"), jsonObject.getString("country"), jsonObject.getString("state"), jsonObject.getString("city"), jsonObject.getString("isp"), jsonObject.getBoolean("proxy"));
    }

}
