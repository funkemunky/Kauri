package dev.brighten.anticheat.classloader;

import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.db.utils.json.JsonReader;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class CheckLicense {

    private static String license = null;
    public static void checkLicense() {
        if(license == null) {
            Plugin loader;
            if((loader = Bukkit.getPluginManager().getPlugin("KauriLoader")) != null) {
                license = loader.getConfig().getString("license");
            } else license = Config.license;
        }

        boolean valid = false;
        try {
            URL url = new URL(getURL("Kauri", license));

            URLConnection connection =  url.openConnection();

            connection.setConnectTimeout(4000);
            connection.setReadTimeout(3000);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String response = JsonReader.readAll(bufferedReader);
            valid = Boolean.parseBoolean(response);

            if(!valid) {
                url = new URL(getURL("Kauri%20Ara", license));

                connection = url.openConnection();

                connection.setConnectTimeout(4000);
                connection.setReadTimeout(3000);

                bufferedReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

                response = JsonReader.readAll(bufferedReader);
                valid = Boolean.parseBoolean(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!valid) {
            MiscUtils.printToConsole("Kauri license is not valid! Disabling...");
            Bukkit.getPluginManager().disablePlugin(Kauri.INSTANCE);
        }
    }

    @SneakyThrows
    public static String getURL(String name, String license) {
        return String.format("https://funkemunky.cc/api/license/custom?name=%s&license=%s",
                name, license);
    }
}
