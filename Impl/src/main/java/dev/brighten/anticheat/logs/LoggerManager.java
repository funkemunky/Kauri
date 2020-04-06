package dev.brighten.anticheat.logs;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.config.MongoConfig;
import dev.brighten.anticheat.logs.data.config.MySQLConfig;
import dev.brighten.anticheat.logs.data.impl.FlatfileStorage;
import dev.brighten.anticheat.logs.data.impl.MongoStorage;
import dev.brighten.anticheat.logs.data.impl.MySQLStorage;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;

import java.util.*;

public class LoggerManager {

    public DataStorage storage;

    public LoggerManager() {
        if(MongoConfig.enabled) {
            storage = new MongoStorage();
        } else if(MySQLConfig.enabled) {
            storage = new MySQLStorage();
        } else storage = new FlatfileStorage();
    }

    public void addLog(ObjectData data, Check check, String info) {
        Log log = new Log(data.uuid , check.name, info, check.vl, data.lagInfo.transPing,
                System.currentTimeMillis(), Kauri.INSTANCE.tps);

        storage.addLog(log);
    }

    public void addPunishment(ObjectData data, Check check) {
        Punishment punishment = new Punishment(data.uuid, check.name, System.currentTimeMillis());

        storage.addPunishment(punishment);
    }

    public List<Log> getLogs(UUID uuid) {
        return storage.getLogs(uuid, null, 0, Integer.MAX_VALUE, 0, System.currentTimeMillis());
    }

    public List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        return storage.getLogs(uuid, check, arrayMin, arrayMax, timeFrom, timeTo);
    }

    public void clearLogs(UUID uuid) {
        storage.removeAll(uuid);
    }
    
    public List<Punishment> getPunishments(UUID uuid) {
        return storage.getPunishments(uuid, 0, Integer.MAX_VALUE, 0, System.currentTimeMillis());
    }

    public List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        return storage.getPunishments(uuid, arrayMin, arrayMax, timeFrom, timeTo);
    }

    public Map<UUID, List<Log>> getLogsWithinTimeFrame(long timeFrame) {
        long currentTime = System.currentTimeMillis();

        Map<UUID, List<Log>> logs = new HashMap<>();

        getLogs(null, null, 0, Integer.MAX_VALUE,
                currentTime - timeFrame, currentTime + 100)
                .forEach(log -> {
                    List<Log> logsList = logs.getOrDefault(log.uuid, new ArrayList<>());

                    logsList.add(log);

                    logs.put(log.uuid, logsList);
                });

        return logs;
    }

}