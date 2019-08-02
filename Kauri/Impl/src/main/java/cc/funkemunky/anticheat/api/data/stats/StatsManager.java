package cc.funkemunky.anticheat.api.data.stats;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.Priority;
import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

import static cc.funkemunky.anticheat.api.data.logging.LoggerManager.*;

@Getter
@Init(priority = Priority.HIGH)
public class StatsManager {
    private int flagged;
    private int banned;

    @ConfigSetting(path = "data.stats", name = "type")
    public static String type = "FLATFILE";

    private Database database;

    public StatsManager() {
        flagged = 0;
        banned = 0;
        Carbon carbon = Atlas.getInstance().getCarbon();
        switch(type.toLowerCase()) {
            case "flatfile": {
                carbon.createFlatfileDatabase(Kauri.getInstance().getDataFolder().getPath() + File.separator + "dbs", "KauriStats");
                MiscUtils.printToConsole("&aCreated Flatfile DB!");
                break;
            }
            case "mysql": {
                carbon.createSQLDatabase("KauriStats", mySQLIp, mySQLUsername, mySQLPassword, mySQLDatabase, mySQLPort);
                MiscUtils.printToConsole("&aConnected to MySQL!");
                break;
            }
            case "mongo": {
                carbon.initMongo(mongoIp, mongoPort, mongoDatabase, mongoUsername, mongoPassword);
                carbon.createMongoDatabase("KauriStats");
                MiscUtils.printToConsole("&aConnected to MongoDB!");
                break;
            }
            default: {
                Bukkit.getLogger().log(Level.SEVERE, "Database type \"" + type + "\" is not a valid database format! Logging functionality is disabled.");
                enabled = false;
                return;
            }
        }
        database = carbon.getDatabase("KauriStats");
        loadStats();
    }

    public void saveStats() {
        database.getDatabaseValues().put("Kauri;flagged", flagged);
        database.getDatabaseValues().put("Kauri;banned", banned);
        database.saveDatabase();
    }

    public void loadStats() {
        database.loadDatabase();
        flagged = (int) database.getDatabaseValues().getOrDefault("Kauri;flagged", 0);
        banned = (int) database.getDatabaseValues().getOrDefault("Kauri;banned", 0);
    }

    public void resetStats() {
        flagged = banned = 0;
        database.getDatabaseValues().clear();
        database.saveDatabase();
    }

    public void addFlag() {
        flagged++;
    }

    public void addFlag(int amount) {
        flagged += amount;
    }

    public void addBan() {
        banned++;
    }

    public void removeBan() {
        banned -= banned > 0 ? 1 : 0;
    }

    public boolean isPlayerBanned(PlayerData data) {
        return Kauri.getInstance().getLoggerManager().isBanned(data.getUuid());
    }

    public boolean isPlayerBanned(UUID uuid) {
        return Kauri.getInstance().getLoggerManager().isBanned(uuid);
    }
}
