package dev.brighten.anticheat.processing.vpn;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.db.flatfile.FlatfileDatabase;
import cc.funkemunky.carbon.db.mongo.MongoDatabase;
import cc.funkemunky.carbon.db.sql.MySQLDatabase;
import cc.funkemunky.carbon.utils.Pair;
import cc.funkemunky.carbon.utils.json.JSONException;
import cc.funkemunky.carbon.utils.json.JSONObject;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.utils.JsonReader;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VPNHandler {

    private Database database;
    public ExecutorService vpnThread;

    public VPNHandler() {
        MiscUtils.printToConsole("&cLoading VPNHandler&7...");
        MiscUtils.printToConsole("&7Setting up Carbon database &eVPN-Cache&7...");
        if(LoggerManager.mongoEnabled) {
            database = new MongoDatabase("VPN-Cache");
        } else if(LoggerManager.mySQLEnabled) {
            database = new MySQLDatabase("VPN-Cache", LoggerManager.sqlDatabase, LoggerManager.sqlPort);
        } else {
            database = new FlatfileDatabase("VPN-Cache");
        }
        MiscUtils.printToConsole("&7Registering listener...");
        vpnThread = Executors.newSingleThreadExecutor();
        Bukkit.getPluginManager().registerEvents(new VPNListener(), Kauri.INSTANCE);
    }

    public VPNResponse getResponse(Player player) {
        return getResponse(player.getAddress().getAddress().getHostAddress());
    }

    public void cacheReponse(VPNResponse response) {
        if(response.isSuccess()) {
            StructureSet set = database.createStructure(response.getIp(),
                    new Pair<>("city", response.getCity()),
                    new Pair<>("countryCode", response.getCountryName()),
                    new Pair<>("isp", response.getIsp()),
                    new Pair<>("state", response.getState()),
                    new Pair<>("proxy", response.isProxy()),
                    new Pair<>("country", response.getCountryName()),
                    new Pair<>("timeStamp", System.currentTimeMillis()));
        }
    }

    public VPNResponse getIfCached(String ipAddress) {
        if(database.contains(ipAddress)) {
            StructureSet set = database.get(ipAddress);
            return new VPNResponse(ipAddress,
                    set.getField("country"),
                    set.getField("countryCode"),
                    set.getField("state"),
                    set.getField("city"),
                    set.getField("isp"),
                    true,
                    set.getField("proxy"));
        } else {
            return null;
        }
    }

    public VPNResponse getResponse(String ipAddress) {
        try {

            val response = getIfCached(ipAddress);

            if(response != null) return response;

            String license = !VPNConfig.license.equals("none")
                    ? (Bukkit.getPluginManager().isPluginEnabled("KauriLoader")
                    ? Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license")
                    : "none") : VPNConfig.license;

            String url = "https://funkemunky.cc/vpn?license=" + license + "&ip=" + ipAddress;

            JSONObject object = JsonReader.readJsonFromUrl(url);

            if (!object.has("ip")) {
                return null;
            }

            val toCacheAndReturn = VPNResponse.fromJson(object.toString());

            cacheReponse(toCacheAndReturn);

            return toCacheAndReturn;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}