package cc.funkemunky.anticheat.api.data.logging;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.database.Database;
import cc.funkemunky.api.database.DatabaseType;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Init(priority = Priority.HIGH)
public class LoggerManager {
    private Map<UUID, Map<String, Integer>> violations;

    @ConfigSetting(path = "data.logging", name = "type")
    public String type = "FLATFILE";

    public LoggerManager() {
        violations = new ConcurrentHashMap<>();
        Atlas.getInstance().getDatabaseManager().createDatabase("KauriLogs", DatabaseType.valueOf(type));
    }

    public void loadFromDatabase() {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");

        database.loadDatabase();

        database.getDatabaseValues().keySet().forEach(key -> {
            String[] toFormat = key.split(";");

            if (!toFormat[1].equals("banned")) {
                UUID uuid = UUID.fromString(toFormat[0]);

                Map<String, Integer> vls = violations.getOrDefault(uuid, new HashMap<>());

                int vl = (int) database.getDatabaseValues().get(key);
                vls.put(toFormat[1], vl);

                violations.put(uuid, vls);
            }
        });
    }

    public void saveToDatabase() {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");

        violations.keySet().forEach(key -> {
            Map<String, Integer> vls = violations.get(key);

            vls.keySet().forEach(check -> {
                int vl = vls.get(check);

                database.inputField(key.toString() + ";" + check, vl);
            });
        });
        database.saveDatabase();
    }

    public void addViolation(UUID uuid, Check check) {
        addAndGetViolation(uuid, check, 1);
    }

    public void addBan(UUID uuid, Check check) {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");

        database.inputField(uuid.toString() + ";banned", check.getName());
    }

    public boolean isBanned(UUID uuid) {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");
        return database.getDatabaseValues().keySet().stream().anyMatch(key -> key.startsWith(uuid.toString()));
    }

    public void removeBan(UUID uuid) {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");

        database.getDatabaseValues().remove(uuid.toString() + ";banned");

        Kauri.getInstance().getStatsManager().removeBan();
    }


    public String getBanReason(UUID uuid) {
        Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");

        Optional<String> reasonOp = database.getDatabaseValues().keySet().stream().filter(key -> key.startsWith(uuid.toString())).findFirst();

        return reasonOp.orElse("none");
    }

    public int addAndGetViolation(UUID uuid, Check check) {
        return addAndGetViolation(uuid, check, 1);
    }

    public int addAndGetViolation(UUID uuid, Check check, int amount) {
        if (!violations.containsKey(uuid)) {
            violations.put(uuid, new HashMap<>());
        }

        Map<String, Integer> vls = violations.get(uuid);

        int vio;
        vls.put(check.getName(), (vio = (vls.getOrDefault(check.getName(), 0) + amount)));

        violations.put(uuid, vls);

        return vio;
    }

    public void clearLogs(UUID uuid) {
        Atlas.getInstance().getThreadPool().execute(() -> {
            Database database = Atlas.getInstance().getDatabaseManager().getDatabase("KauriLogs");
            database.getDatabaseValues().keySet().stream().filter(key -> key.startsWith(uuid.toString())).forEach(key -> database.getDatabaseValues().remove(key));
            database.saveDatabase();
        });
    }

    public Map<String, Integer> getViolations(UUID uuid) {
        return violations.getOrDefault(uuid, new HashMap<>());
    }

    public int getViolations(Check check, UUID uuid) {
        Map<String, Integer> vls = getViolations(uuid);

        return vls.getOrDefault(check.getName(), 0);
    }
}
