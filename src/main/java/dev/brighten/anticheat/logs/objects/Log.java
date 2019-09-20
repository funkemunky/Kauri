package dev.brighten.anticheat.logs.objects;

import cc.funkemunky.api.utils.json.JSONException;
import cc.funkemunky.api.utils.json.JSONObject;

public class Log {

    public String checkName;
    public double vl;
    public long ping, timeStamp;
    public double tps;

    public Log(String checkName, double vl, long ping, long timeStamp, double tps) {
        this.checkName = checkName;
        this.vl = vl;
        this.ping = ping;
        this.timeStamp = timeStamp;
        this.tps = tps;
    }

    public static Log fromJson(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);

            return new Log(object.getString("checkName"),
                    object.getDouble("vl"),
                    object.getLong("ping"),
                    object.getLong("timeStamp"),
                    object.getDouble("tps"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("checkName", checkName);
            object.put("vl", vl);
            object.put("ping", ping);
            object.put("timeStamp", timeStamp);
            object.put("tps", tps);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }
}
