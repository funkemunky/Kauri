package dev.brighten.anticheat.logs.objects;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import dev.brighten.db.utils.json.JSONException;
import dev.brighten.db.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class Log {

    public UUID uuid;
    public String checkName, info;
    public float vl;
    public int ping;
    public long timeStamp;
    public double tps;
    private static WrappedClass logClass = new WrappedClass(Log.class);

    public static Log fromJson(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            Log log = new Log();

            for (WrappedField field : logClass.getFields(true)) {
                field.set(log, object.get(field.getField().getName()));
            }

            return log;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toJson() {
        JSONObject object = new JSONObject();

        try {
            for (WrappedField field : logClass.getFields(true)) {
                object.put(field.getField().getName(), (Object)field.get(this));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }
}
