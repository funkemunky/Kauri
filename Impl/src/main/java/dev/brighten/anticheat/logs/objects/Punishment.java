package dev.brighten.anticheat.logs.objects;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.utils.json.JSONException;
import cc.funkemunky.api.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class Punishment {
    public UUID uuid;
    public String checkName;
    public long timeStamp;
    private static WrappedClass punishClass = new WrappedClass(Punishment.class);

    public static Punishment fromJson(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            Punishment log = new Punishment();

            for (WrappedField field : punishClass.getFields(true)) {
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
            for (WrappedField field : punishClass.getFields(true)) {
                object.put(field.getField().getName(), (Object)field.get(this));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }
}
