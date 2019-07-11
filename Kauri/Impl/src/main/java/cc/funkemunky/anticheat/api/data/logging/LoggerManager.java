package cc.funkemunky.anticheat.api.data.logging;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.api.database.DatabaseType;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Init(priority = Priority.HIGH)
@NoArgsConstructor
public class LoggerManager {
    @Getter
    private Map<UUID, List<Violation>> violations = new ConcurrentHashMap<>();

    @Getter
    private Set<UUID> recentViolators = new LinkedHashSet<>();

    @ConfigSetting(path = "data.logging", name = "type")
    public String type = "FLATFILE";
    
    @ConfigSetting(path = "data.logging", name = "enabled")
    public boolean enabled = true;
    
    @ConfigSetting(path = "database.mysql", name = "ip")
    private String mySQLIp = "localhost";
    
    @ConfigSetting(path = "database.mysql", name = "port")
    private int mySQLPort = 3306;
    
    @ConfigSetting(path = "database.mysql", name = "username")
    private String mySQLUsername = "username";
    
    @ConfigSetting(path = "database.mysql", name = "password")
    private String mySQLPassword = "password";
    
    @ConfigSetting(path = "database.mysql", name = "database")
    private String mySQLDatabase = "Kauri";

    @ConfigSetting(path = "database.mongo", name = "ip")
    private String mongoIp = "localhost";

    @ConfigSetting(path = "database.mongo", name = "port")
    private int mongoPort = 27107;

    @ConfigSetting(path = "database.mongo", name = "username")
    private String mongoUsername = "username";

    @ConfigSetting(path = "database.mongo", name = "password")
    private String mongoPassword = "password";

    @ConfigSetting(path = "database.mongo", name = "database")
    private String mongoDatabase = "Kauri";
    
    public Database database;

    public LoggerManager(Carbon carbon) {
        if(!enabled) return;
        switch(type.toLowerCase()) {
            case "flatfile": {
                carbon.createFlatfileDatabase(Kauri.getInstance().getDataFolder().getPath() + File.pathSeparator + "dbs", "KauriLogs");
                break;
            }
            case "mysql": {
                carbon.createSQLDatabase("KauriLogs", mySQLIp, mySQLUsername, mySQLPassword, mySQLDatabase, mySQLPort);
                break;
            }
            case "mongo": {
                carbon.initMongo(mongoIp, mongoPort, mongoDatabase, mongoUsername, mongoPassword);
                carbon.createMongoDatabase("KauriLogs");
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

        violations = database.getDatabaseValues().keySet().stream().collect(Collectors.toMap(UUID::fromString, key -> {
            String[] split = ((String)database.getDatabaseValues().get(key)).split("@");

            return Arrays.stream(split).map(Violation::fromJson).collect(Collectors.toList());
        }));
    }

    public void saveToDatabase() {
        if(!enabled) return;
        MiscUtils.printToConsole(Color.Green + "Saving logs to database...");
        Database database = this.database;

        database.getDatabaseValues().clear();
        violations.keySet().forEach(key -> {
            List<Violation> vls = violations.get(key);

            StringBuilder builder = new StringBuilder();
            vls.stream().map(Violation::toJson).forEach(string -> builder.append(string).append("@"));
            builder.deleteCharAt(builder.length() - 1);

            database.inputField(key.toString(), builder.toString());
        });

        database.saveDatabase();
    }

    public void addViolation(PlayerData data, Check check, String info, AlertTier tier) {
        if(enabled) {
            List<Violation> violations = this.violations.getOrDefault(data.getUuid(), new ArrayList<>());

            violations.add(new Violation(check.getName(), info, Kauri.getInstance().getTps(), data.getTransPing(), System.currentTimeMillis(), tier));

            this.violations.put(data.getUuid(), violations);
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
                Database database = this.database;
                database.getDatabaseValues().keySet().stream().filter(key -> key.startsWith(uuid.toString())).forEach(key -> database.getDatabaseValues().remove(key));
                database.saveDatabase();
            });
        }
    }

    public Map<String, Integer> getViolations(UUID uuid) {
        if(enabled) {
            if(violations.containsKey(uuid)) {
                List<Violation> vlList = violations.get(uuid);

                Map<String, Integer> vls = new HashMap<>();

                vlList.stream().forEach(vl -> vls.put(vl.getCheckName(), vls.getOrDefault(vl.getCheckName(), 0) + 1));

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
