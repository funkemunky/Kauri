package cc.funkemunky.anticheat.api.log;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class LoggerManager {
    private MongoCollection<Document> logCollection;
    private Map<UUID, Log> logs;
    private List<UUID> recentlyFlagged = new ArrayList<>();
    private LogHandler logHandler;

    public LoggerManager() {
        logCollection = Kauri.getInstance().getMongo().getMongoDatabase().getCollection("violations");
        logs = new ConcurrentHashMap<>();

        loadLogs();
        logHandler = new LogHandler();
    }

    public void loadLogs() {
        logCollection.find().forEach((Block<? super Document>) document -> {
            if(document.keySet().contains("uuid")) {
                logs.put(UUID.fromString(document.getString("uuid")), getLogFromDocument(document));
            }
        });
    }

    public Log getLogFromDocument(Document document) {
        UUID uuid = UUID.fromString(document.getString("uuid"));
        Map<String, Integer> map = new HashMap<>();

        document.keySet().stream().filter(key -> Kauri.getInstance().getCheckManager().isCheck(key)).forEach(key -> {
            map.put(key, document.getInteger(key));
        });

        Log log = new Log(uuid);

        log.setViolations(map);
        log.setBanned(document.getBoolean("banned"));
        log.setBannedCheck(document.getString("bannedCheck"));

        Map<String, List<String>> toSort = new HashMap<>();

        document.keySet().stream().filter(key -> key.split("-").length == 3).forEach(key -> {
            String alert = document.getString(key);
            String[] splitKey = key.split("-");

            List<String> alerts = toSort.getOrDefault(splitKey[1], new ArrayList<>());
            alerts.add(splitKey[2] + "~~" + alert);
            toSort.put(splitKey[1], alerts);
        });

        for (String key : toSort.keySet()) {
            List<String> listToSort = toSort.get(key), finalList = new ArrayList<>();

            listToSort.stream().sorted(Comparator.comparingInt(val -> Integer.parseInt(val.split("~~")[0]))).forEachOrdered(val -> finalList.add(val.split("~~")[1]));

            log.getAlertLog().put(key, finalList);
        }

        return log;
    }

    public Document getDocumentFromLog(Log log) {
        Document document = new Document("uuid", log.getUuid().toString());

        log.getViolations().keySet().forEach(key -> document.put(key, log.getViolations().get(key)));

        document.put("banned", log.isBanned());
        document.put("bannedCheck", log.getBannedCheck());

        log.getAlertLog().keySet().forEach(key -> {
            List<String> alertLog = log.getAlertLog().get(key);

            for (int i = 0; i < alertLog.size(); i++) {
                document.put("alertLog-" + key + "-" + i, alertLog.get(i));
            }
        });

        return document;
    }

    public void addAlertToLog(PlayerData data, Check check, String alert) {
        Log log = logs.getOrDefault(data.getUuid(), new Log(data.getUuid()));

        List<String> alerts = log.getAlertLog().getOrDefault(check.getName(), new ArrayList<>());

        alerts.add(alert);

        log.getAlertLog().put(check.getName(), alerts);
    }

    public Log addViolationsToLog(PlayerData data) {
        Log log = logs.getOrDefault(data.getUuid(), new Log(data.getUuid()));

        data.getChecks().stream().filter(check -> check.getVl() > 0).forEach(check -> {
            int vl = log.getViolations().getOrDefault(check.getName(), 0) + check.getVl();

            log.getViolations().put(check.getName(), vl);

            check.setVl(0);
        });

        return log;
    }

    public Log addViolationToLog(Check check, UUID uuid) {
        Log log = logs.getOrDefault(uuid, new Log(uuid));

        int vl = log.getViolations().getOrDefault(check.getName(), 0) + check.getVl();
        log.getViolations().put(check.getName(), vl);
        recentlyFlagged.remove(uuid);
        recentlyFlagged.add(0, uuid);
        check.setVl(0);

        return log;
    }

    public void addViolationsToLogAndSave(PlayerData data) {
        logs.put(data.getUuid(), addViolationsToLog(data));
    }

    public int addViolationToLogAndSave(Check check, UUID uuid) {
        return logs.put(uuid, addViolationToLog(check, uuid)).getViolations().getOrDefault(check.getName(), 0);
    }

    public void setBanned(UUID uuid, Check check) {
        Log log = logs.getOrDefault(uuid, new Log(uuid));

        log.setBanned(true);
        log.setBannedCheck(check.getName());

        logs.put(uuid, log);
    }

    public void saveAllLogs() {
        logs.values().forEach(this::saveLog);
    }

    public void saveLog(Log log) {
        logCollection.deleteOne(Filters.eq("uuid", log.getUuid().toString()));
        logCollection.insertOne(getDocumentFromLog(log));
    }
}
