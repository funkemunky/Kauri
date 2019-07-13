package cc.funkemunky.anticheat.api.data.logging;

import cc.funkemunky.anticheat.api.checks.AlertTier;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;
import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    }

    public void loadFromDatabase() {

    }

    public void saveToDatabase() {

    }

    public void addViolation(PlayerData data, Check check, String info, AlertTier tier) {

    }

    public void addBan(UUID uuid, Check check) {

    }

    public boolean isBanned(UUID uuid) {
        if(!enabled) {
            return false;
        }
        Database database = this.database;
        return database.getDatabaseValues().keySet().stream().anyMatch(key -> key.equals(uuid.toString() + ";banned"));
    }

    public void removeBan(UUID uuid) {

    }


    public String getBanReason(UUID uuid) {

        return "none";
    }

    public void clearLogs(UUID uuid) {

    }

    public Map<String, Integer> getViolations(UUID uuid) {

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
