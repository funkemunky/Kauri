package dev.brighten.api;

import cc.funkemunky.api.reflections.Reflections;
import cc.funkemunky.api.reflections.types.WrappedClass;
import dev.brighten.api.handlers.ExemptHandler;
import dev.brighten.api.wrappers.WrappedDataManager;
import dev.brighten.api.wrappers.WrappedKauri;
import dev.brighten.db.utils.json.JSONException;
import dev.brighten.db.utils.json.JSONObject;
import dev.brighten.db.utils.json.JsonReader;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class KauriAPI {

    public static KauriAPI INSTANCE;

    public ExemptHandler exemptHandler;
    public ScheduledExecutorService service;
    public WrappedDataManager dataManager;
    WrappedKauri kauriPlugin;
    public JSONObject object;


    public KauriAPI() {
        INSTANCE = this;
        exemptHandler = new ExemptHandler();
        service = Executors.newSingleThreadScheduledExecutor();

        try {
            InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream("classes.json");


            object = new JSONObject(JsonReader.readAll(new BufferedReader(new InputStreamReader(stream))));
            WrappedClass kauriClass = Reflections.getClass(object.getString("kauriMain"));
            kauriPlugin = new WrappedKauri(kauriClass,
                    kauriClass.getFieldByType(kauriClass.getParent(), 0).get(null));
            dataManager = kauriPlugin.getDataManager();
        } catch (IOException | JSONException e) {
            object = null;
            e.printStackTrace();
        }
    }

    public void reloadChecksForPlayer(Player player) {
        dataManager.getData(player).reloadChecks();
    }
}
