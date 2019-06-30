package cc.funkemunky.anticheat.api.data.logging;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.database.Database;
import cc.funkemunky.api.database.DatabaseType;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;
import lombok.Getter;
import lombok.val;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Init(priority = Priority.HIGH)
public class LoggerManager {
    @Getter
    private Map<UUID, Violation> violations = new ConcurrentHashMap<>();

    @Getter
    private Set<UUID> recentViolators = new LinkedHashSet<>();

    @ConfigSetting(path = "data.logging", name = "type")
    public String type = "FLATFILE";

    public void loadFromDatabase() {

    }

    public void saveToDatabase() {

    }

    public void addViolation(UUID uuid, Check check) {

    }

    public void addBan(UUID uuid, Check check) {

    }

    public boolean isBanned(UUID uuid) {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");
        return database.getDatabaseValues().keySet().stream().anyMatch(key -> key.equals(uuid.toString() + ";banned"));
    }

    public void removeBan(UUID uuid) {

    }


    public String getBanReason(UUID uuid) {
        return "none";
    }

    public int addAndGetViolation(UUID uuid, Check check) {
        return addAndGetViolation(uuid, check, 1);
    }

    public int addAndGetViolation(UUID uuid, Check check, int amount) {
        return amount;
    }

    public void clearLogs(UUID uuid) {

    }

    public Map<String, Integer> getViolations(UUID uuid) {
        return violations.getOrDefault(uuid, new Violation()).getViolations();
    }

    public int getViolations(Check check, UUID uuid) {
        Map<String, Integer> vls = getViolations(uuid);

        return vls.getOrDefault(check.getName(), 0);
    }
}
