package dev.brighten.anticheat.logs;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.Structure;
import cc.funkemunky.carbon.db.StructureSet;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;

import java.util.*;
import java.util.stream.Collectors;

@Init
public class LoggerManager {

    public Database logsDatabase;

    public LoggerManager() {

    }

    /*My SQL */
    @ConfigSetting(path = "database.mysql", name = "enabled")
    private static boolean mySQLEnabled = false;

    @ConfigSetting(path = "database.mysql", name = "username")
    private static String sqlUsername = "root";

    @ConfigSetting(path = "database.mysql", name = "database")
    private static String sqlDatabase = "Kauri";

    @ConfigSetting(path = "database.mysql", name = "password")
    private static String sqlPassword = "password";

    @ConfigSetting(path = "database.mysql", name = "ip")
    private static String sqlIp = "127.0.0.1";

    @ConfigSetting(path = "database.mysql", name = "port")
    private static int sqlPort = 3306;

    /* Mongo */
    @ConfigSetting(path = "database.mongo", name = "enabled")
    private static boolean mongoEnabled = false;

    @ConfigSetting(path = "database.mongo", name = "username")
    private static String mongoUsername = "root";

    @ConfigSetting(path = "database.mongo", name = "password")
    private static String mongoPassword = "password";

    @ConfigSetting(path = "database.mongo", name = "database")
    private static String mongoDatabase = "Kauri";

    @ConfigSetting(path = "database.mongo", name = "ip")
    private static String mongoIp = "127.0.0.1";

    @ConfigSetting(path = "database.mongo", name = "port")
    private static int mongoPort = 27017;

    public LoggerManager(boolean aLittleStupid) {
        if(aLittleStupid) {
            if(mySQLEnabled) {
                MiscUtils.printToConsole("&7Setting up SQL...");
                Atlas.getInstance().getCarbon().createSQLDatabase(sqlDatabase, sqlIp, sqlPort, sqlUsername, sqlPassword);
            } else if(mongoEnabled) {
                MiscUtils.printToConsole("&7Setting up Mongo...");
                Atlas.getInstance().getCarbon().initMongo(mongoDatabase, mongoIp, mongoPort, mongoUsername, mongoPassword);
                Atlas.getInstance().getCarbon().createMongoDatabase("logs");
            } else {
                MiscUtils.printToConsole("&7Setting up FlatfileDB...");
                Atlas.getInstance().getCarbon().createFlatfileDatabase(Kauri.INSTANCE.getDataFolder().getPath(), "logs");
            }
            logsDatabase = Atlas.getInstance().getCarbon().getDatabase(mySQLEnabled ? sqlDatabase : "logs");
            MiscUtils.printToConsole("&7Loading database...");
            logsDatabase.loadDatabase();
            save();
        }
    }

    public void addLog(ObjectData data, Check check, String info) {
        Log log = new Log(check.name, info, check.vl, data.lagInfo.transPing, System.currentTimeMillis(), Kauri.INSTANCE.tps);

        StructureSet set = logsDatabase.createStructureSet(
                new Structure("uuid", data.uuid.toString()),
                new Structure("checkName", log.checkName),
                new Structure("vl", log.vl),
                new Structure("info", log.info),
                new Structure("ping", log.ping),
                new Structure("timeStamp", log.timeStamp),
                new Structure("tps", log.tps));

        logsDatabase.inputField(set);
    }

    public void addPunishment(ObjectData data, Check check) {
        Punishment punishment = new Punishment(data.uuid, check.name, System.currentTimeMillis());
        
        StructureSet set = logsDatabase.createStructureSet(
                new Structure("type", "punishment"),
                new Structure("uuid", data.uuid.toString()), 
                new Structure("checkName", punishment.checkName),
                new Structure("timeStamp", punishment.timeStamp));
        
        logsDatabase.inputField(set);
    }

    public List<Log> getLogs(UUID uuid) {
        List<StructureSet> sets = logsDatabase.getFieldsByStructure(struct ->
                struct.name.equals("uuid")
                        && String.valueOf(struct.object).equals(uuid.toString()));

        return sets.stream().map(set -> new Log(
                String.valueOf(set.getStructureByName("checkName").get().object),
                String.valueOf(set.getStructureByName("info").get().object),
                (double)set.getStructureByName("vl").get().object,
                (long)set.getStructureByName("ping").get().object,
                (long)set.getStructureByName("timeStamp").get().object,
                (double)set.getStructureByName("tps").get().object)).collect(Collectors.toList());
    }
    
    public List<Punishment> getPunishments(UUID uuid) {
        List<StructureSet> structureSets = logsDatabase.getFieldsByStructure(
                (struct -> struct.name.equals("type") && String.valueOf(struct.object).equals("punishment")),
                (struct -> struct.name.equals("uuid") && UUID.fromString(String.valueOf(struct.object)).equals(uuid)));

        return structureSets.stream().map(set -> new Punishment(uuid,
                String.valueOf(set.getStructureByName("checkName").get().object),
                (long)set.getStructureByName("timeStamp").get().object)).collect(Collectors.toList());
    }

    public Map<UUID, List<Log>> getLogsWithinTimeFrame(long timeFrame) {
        long currentTime = System.currentTimeMillis();

        Map<UUID, List<Log>> logs = new HashMap<>();

        logsDatabase.getDatabaseValues().stream().filter(set -> {
            Optional<Structure> optional = set.getStructureByName("timeStamp");

            return optional.isPresent() && (currentTime - (long)optional.get().object) < timeFrame;
        }).forEach(set -> {
            UUID uuid = UUID.fromString((String)set.getStructureByName("uuid").get().object);
            List<Log> logList = logs.getOrDefault(uuid, new ArrayList<>());

            logList.add(new Log(
                    String.valueOf(set.getStructureByName("checkName").get().object),
                    String.valueOf(set.getStructureByName("info").get().object),
                    (double)set.getStructureByName("vl").get().object,
                    (long)set.getStructureByName("ping").get().object,
                    (long)set.getStructureByName("timeStamp").get().object,
                    (double)set.getStructureByName("tps").get().object));

            logs.put(uuid, logList);
        });

        logs.values().forEach(list -> list.sort(Comparator.comparing(log -> currentTime - log.timeStamp)));

        return logs;
    }

    private void save() {
        RunUtils.taskTimerAsync(() -> logsDatabase.saveDatabase(), 6000, 6000);
    }

}