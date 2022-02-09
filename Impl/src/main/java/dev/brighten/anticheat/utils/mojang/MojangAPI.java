package dev.brighten.anticheat.utils.mojang;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.db.utils.json.JSONArray;
import dev.brighten.db.utils.json.JSONException;
import dev.brighten.db.utils.json.JSONObject;
import dev.brighten.db.utils.json.JsonReader;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;

public class MojangAPI {

    public static String getUsername(UUID uuid) {
        String name = Kauri.INSTANCE.loggerManager.storage.getNameFromUUID(uuid);

        if (name == null) {
            name = lookupName(uuid);

            if(name != null) {
                Kauri.INSTANCE.loggerManager.storage.cacheAPICall(uuid, name);
            }
        }

        return name;
    }

    public static UUID getUUID(String name) {
        UUID uuid = Kauri.INSTANCE.loggerManager.storage.getUUIDFromName(name);

        if(uuid == null) {
            uuid = lookupUUID(name);

            if(uuid != null) {
                Kauri.INSTANCE.loggerManager.storage.cacheAPICall(uuid, name);
            }
        }
        return uuid;
    }

    public static UUID formatFromMojangUUID(String mojangUUID) {
        String uuid = "";
        for(int i = 0; i <= 31; i++) {
            uuid = uuid + mojangUUID.charAt(i);
            if(i == 7 || i == 11 || i == 15 || i == 19) {
                uuid = uuid + "-";
            }
        }

        return UUID.fromString(uuid);
    }

    public static String formatToMojangUUID(String uuid) {
        return uuid.replace("-", "");
    }

    public static String formatToMojangUUID(UUID uuid) {
        return formatToMojangUUID(uuid.toString());
    }

    public static UUID lookupUUID(String playername) {
        if (!Config.noPremiumUUID) {
            try {
                JSONObject object = JsonReader
                        .readJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + playername);

                if (object.has("id")) {
                    UUID uuid = formatFromMojangUUID(object.getString("id"));

                    return uuid;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                //Empty catch clause
            }
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(playername);

        if(player != null) return player.getUniqueId();
        return null;
    }

    private static void readData(String toRead, StringBuilder result) {
        int i = 7;

        while (i < 200) {
            if (!String.valueOf(toRead.charAt(i)).equalsIgnoreCase("\"")) {

                result.append(String.valueOf(toRead.charAt(i)));

            } else {
                break;
            }

            i++;
        }
    }

    private static String lookupName(UUID id) {
        if (id == null) {
            return null;
        } else {
            try {
                URLConnection conn = new URL("https://api.mojang.com/user/profiles/"
                        + formatToMojangUUID(id) + "/names").openConnection();

                conn.setConnectTimeout(2000);
                conn.setReadTimeout(3000);

                InputStream is = conn.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

                String jsonText = JsonReader.readAll(rd);

                val array = new JSONArray(jsonText);
                return array.getJSONObject(array.length() - 1).getString("name");
            } catch (MalformedURLException var12) {
                Kauri.INSTANCE.getLogger().warning("Malformed URL in UUID lookup");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
