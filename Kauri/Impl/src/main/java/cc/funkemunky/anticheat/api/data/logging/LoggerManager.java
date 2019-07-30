package cc.funkemunky.anticheat.api.data.logging;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.*;
import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Init(priority = Priority.HIGHEST)
@NoArgsConstructor
public class LoggerManager {
    @Getter
    private Map<UUID, List<Violation>> violations = new ConcurrentHashMap<>();

    @Getter
    private Set<UUID> recentViolators = new LinkedHashSet<>();

    @ConfigSetting(path = "data.logging", name = "type")
    public static String type = "FLATFILE";
    
    @ConfigSetting(path = "data.logging", name = "enabled")
    public static boolean enabled = true;
    
    @ConfigSetting(path = "database.mysql", name = "ip")
    public static String mySQLIp = "localhost";
    
    @ConfigSetting(path = "database.mysql", name = "port")
    public static int mySQLPort = 3306;
    
    @ConfigSetting(path = "database.mysql", name = "username")
    public static String mySQLUsername = "username";
    
    @ConfigSetting(path = "database.mysql", name = "password")
    public static String mySQLPassword = "password";
    
    @ConfigSetting(path = "database.mysql", name = "database")
    public static String mySQLDatabase = "Kauri";

    @ConfigSetting(path = "database.mongo", name = "ip")
    public static String mongoIp = "localhost";

    @ConfigSetting(path = "database.mongo", name = "port")
    public static int mongoPort = 27107;

    @ConfigSetting(path = "database.mongo", name = "username")
    public static String mongoUsername = "username";

    @ConfigSetting(path = "database.mongo", name = "password")
    public static String mongoPassword = "password";

    @ConfigSetting(path = "database.mongo", name = "database")
    public static String mongoDatabase = "Kauri";

    @ConfigSetting(path = "database.logging", name = "printToConsole")
    private static boolean printToConsole = true;
    
    public Database database;

    public LoggerManager(Carbon carbon) {
        if(!enabled) return;
        switch(type.toLowerCase()) {
            case "flatfile": {
                carbon.createFlatfileDatabase(Kauri.getInstance().getDataFolder().getPath() + File.separator + "dbs", "KauriLogs");
                MiscUtils.printToConsole("&aCreated Flatfile DB!");
                break;
            }
            case "mysql": {
                carbon.createSQLDatabase("KauriLogs", mySQLIp, mySQLUsername, mySQLPassword, mySQLDatabase, mySQLPort);
                MiscUtils.printToConsole("&aConnected to MySQL!");
                break;
            }
            case "mongo": {
                carbon.initMongo(mongoIp, mongoPort, mongoDatabase, mongoUsername, mongoPassword);
                carbon.createMongoDatabase("KauriLogs");
                MiscUtils.printToConsole("&aConnected to MongoDB!");
                break;
            }
            default: {
                Bukkit.getLogger().log(Level.SEVERE, "Database type \"" + type + "\" is not a valid database format! Logging functionality is disabled.");
                enabled = false;
                return;
            }
        }
        database = carbon.getDatabase("KauriLogs");
        Kauri.getInstance().getExecutorService().scheduleAtFixedRate(this::saveToDatabase, 1, 5, TimeUnit.MINUTES);
    }

    public void loadFromDatabase() {
        if(!enabled) return;
        Database database = this.database;
        database.loadDatabase();

        violations = new ConcurrentHashMap<>();

        for (String key : database.getDatabaseValues().keySet()) {
            List<Violation> vls;
            UUID uuid;
            if(key.contains("@")) {
                String[] splitKey = key.split("@");
                uuid = UUID.fromString(splitKey[0]);
                vls = violations.getOrDefault(uuid, new ArrayList<>());

                vls.add(Violation.fromJson((String) database.getField(key)));
            } else {
                uuid = UUID.fromString(key);
                vls = violations.getOrDefault(uuid, new ArrayList<>());
                vls.add(Violation.fromJson((String) database.getField(key)));
            }
            violations.put(uuid, vls);
         }

        /*List<String> rawKeys = database.getDatabaseValues().keySet().parallelStream().filter(key -> !key.contains("ban") && key.contains("@")).map(key -> key.split("@")[0]).collect(Collectors.toList());
        for (String rawKey : rawKeys) {
            int i = 0;
            List<Violation> violations = new ArrayList<>();
            while(database.getDatabaseValues().containsKey(rawKey + "@" + i)) {
                String field = (String)database.getField(rawKey + "@" + i);
                violations.add(Violation.fromJson(field));
                i++;
            }

            this.violations.put(UUID.fromString(rawKey), violations);
        }

        List<String> normalKeys = database.getDatabaseValues().keySet().parallelStream().filter(key -> !key.contains("ban") && !key.contains("@")).collect(Collectors.toList());

        for(String normalKey : normalKeys) {
            List<Violation> violations = this.violations.getOrDefault(UUID.fromString(normalKey), new ArrayList<>());

            violations.add(Violation.fromJson((String)database.getField(normalKey)));
        }*/
    }

    public void saveToDatabase() {
        if(!enabled) return;
        if(printToConsole) MiscUtils.printToConsole(Color.Green + "Saving logs to database...");
        Database database = this.database;

        database.getDatabaseValues().clear();
        violations.keySet().forEach(key -> {
            List<Violation> vls = violations.get(key);

            List<String> jsonStrings = vls.stream().map(Violation::toJson).collect(Collectors.toList());

            for (int i = 0; i < jsonStrings.size(); i++) {
                database.inputField(key.toString() + "@" + i, jsonStrings.get(i));
            }
        });

        database.saveDatabase();
    }

    public void addViolation(PlayerData data, Check check, String info, AlertTier tier) {
        if(enabled) {
            List<Violation> violations = this.violations.getOrDefault(data.getUuid(), new ArrayList<>());

            violations.add(new Violation(check.getName(), info, Kauri.getInstance().getTps(), data.getTransPing(), System.currentTimeMillis(), tier));

            this.violations.put(data.getUuid(), violations);

            this.recentViolators.add(data.getPlayer().getUniqueId());
        }
    }

    public void addBan(UUID uuid, Check check) {
        if(enabled) {
            Database database = this.database;

            database.inputField(uuid.toString() + ";banned", check.getName());
        }
    }

    public boolean isBanned(UUID uuid) {
        if(!enabled) {
            return false;
        }
        Database database = this.database;
        return database.getDatabaseValues().keySet().stream().anyMatch(key -> key.equals(uuid.toString() + ";banned"));
    }

    public void removeBan(UUID uuid) {
        if(enabled) {
            Database database = this.database;

            database.getDatabaseValues().remove(uuid.toString() + ";banned");

            Kauri.getInstance().getStatsManager().removeBan();
        }
    }


    public String getBanReason(UUID uuid) {
        if(enabled) {
            Database database = this.database;

            Optional<String> reasonOp = database.getDatabaseValues().keySet().stream().filter(key -> key.equals(uuid.toString() + ";banned")).findFirst();

            val key = reasonOp.orElse("none");

            if(!key.equals("none")) {
                return (String) database.getField(key);
            }
        }
        return "none";
    }

    public void clearLogs(UUID uuid) {
        if(enabled) {
            Atlas.getInstance().getService().execute(() -> {
                violations.remove(uuid);
                database.saveDatabase();
            });
        }
    }

    public void clearLogs(UUID uuid, String specifics) {
        if(enabled && violations.containsKey(uuid)) {
            Atlas.getInstance().getService().execute(() -> {
                List<Violation> vls = new CopyOnWriteArrayList<>(violations.get(uuid));

                vls.parallelStream().filter(vl -> vl.getCheckName().equalsIgnoreCase(specifics)).forEach(vls::remove);

                violations.put(uuid, vls);
                database.saveDatabase();
            });
        }
    }

    public Map<String, Integer> getViolations(UUID uuid) {
        if(enabled) {
            if(violations.containsKey(uuid)) {
                List<Violation> vlList = violations.get(uuid);

                Map<String, Integer> vls = new HashMap<>();

                for (Violation vl : vlList) {
                    vls.put(vl.getCheckName(),
                            vls.getOrDefault(vl.getCheckName(), 0) + 1);
                }

                return vls;
            }
        }
        return new HashMap<>();
    }

    public List<Violation> getDetailedViolations(UUID uuid) {
        if(!enabled) return new ArrayList<>();
        return violations.getOrDefault(uuid, new ArrayList<>());
    }

    public int getViolations(Check check, UUID uuid) {
        if(!enabled) return 0;
        Map<String, Integer> vls = getViolations(uuid);

        return vls.getOrDefault(check.getName(), 0);
    }
}
