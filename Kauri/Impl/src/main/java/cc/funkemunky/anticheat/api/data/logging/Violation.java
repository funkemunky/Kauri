package cc.funkemunky.anticheat.api.data.logging;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.utils.json.JSONException;
import cc.funkemunky.anticheat.api.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Violation {
    private String checkName, info;
    private double tps, ping;
    private long timeStamp;
    private AlertTier tier;

    public String toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("check", checkName);
            object.put("info", info.replace(":", "%%"));
            object.put("tier", tier.getName());
            object.put("tps", tps);
            object.put("ping", ping);
            object.put("timeStamp", timeStamp);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Violation fromJson(String json) {
        try {
            JSONObject object = new JSONObject(json);

            return new Violation(object.getString("check"), object.getString("info").replace("%%", ":"), object.getDouble("tps"), object.getDouble("ping"), object.getLong("timeStamp"), AlertTier.getByName(object.getString("tier")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
