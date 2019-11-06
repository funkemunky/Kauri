package dev.brighten.anticheat.logs;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.db.flatfile.FlatfileDatabase;
import cc.funkemunky.carbon.db.mongo.MongoDatabase;
import cc.funkemunky.carbon.db.sql.MySQLDatabase;
import cc.funkemunky.carbon.utils.Pair;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Init
@NoArgsConstructor
public class LoggerManager {

    public Database logsDatabase;

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

    @ConfigSetting(path = "database.mongo", name = "requiresLoginDetails")
    private static boolean mongoLoginDetails = false;

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
                MySQLDatabase.setCredentials(sqlIp, sqlUsername, sqlPassword);
                logsDatabase = new MySQLDatabase("logs", sqlDatabase, sqlPort);
            } else if(mongoEnabled) {
                MiscUtils.printToConsole("&7Setting up Mongo...");
                if(mongoLoginDetails) {
                    logsDatabase = new MongoDatabase("logs",
                            MongoDatabase.initMongo(mongoDatabase, mongoIp, mongoPort, mongoUsername, mongoPassword));
                } else {
                    logsDatabase = new MongoDatabase("logs",
                            MongoDatabase.initMongo(mongoDatabase, mongoIp, mongoPort));
                }
            } else {
                MiscUtils.printToConsole("&7Setting up FlatfileDB...");
                logsDatabase = new FlatfileDatabase("logs");
                //Atlas.getInstance().getCarbon().createFlatfileDatabase(Kauri.INSTANCE.getDataFolder().getPath(), "logs");
            }
            MiscUtils.printToConsole("&7Loading database on second thread...");
            logsDatabase.loadDatabase();
            save();
        }
    }

    public void addLog(ObjectData data, Check check, String info) {
        Log log = new Log(check.name, info, check.vl, data.lagInfo.transPing, System.currentTimeMillis(), Kauri.INSTANCE.tps);

        StructureSet set = logsDatabase.createStructure(RandomStringUtils.random(20),
                new Pair<>("type", "log"),
                new Pair<>("uuid", data.uuid.toString()),
                new Pair<>("checkName", log.checkName),
                new Pair<>("vl", log.vl),
                new Pair<>("info", log.info),
                new Pair<>("ping", log.ping),
                new Pair<>("timeStamp", log.timeStamp),
                new Pair<>("tps", log.tps));

        logsDatabase.updateObject(set);
    }

    public void addPunishment(ObjectData data, Check check) {
        Punishment punishment = new Punishment(data.uuid, check.name, System.currentTimeMillis());
        
        StructureSet set = logsDatabase.createStructure(RandomStringUtils.random(20),
                new Pair<>("type", "punishment"),
                new Pair<>("uuid", data.uuid.toString()), 
                new Pair<>("checkName", punishment.checkName),
                new Pair<>("timeStamp", punishment.timeStamp));
        
        logsDatabase.updateObject(set);
    }

    public List<Log> getLogs(UUID uuid) {

        List<StructureSet> sets = logsDatabase.getDatabaseValues()
                .stream()
                .filter(structSet -> structSet.containsKey("type") && structSet.getField("type").equals("log"))
                .filter(structSet -> structSet.getField("uuid").equals(uuid.toString()))
                .collect(Collectors.toList());

        return sets.stream().map(set -> new Log(
                    String.valueOf(set.getField("checkName")),
                    String.valueOf(set.getField("info")),
                    set.getField("vl"),
                    set.getField("ping"),
                    set.getField("timeStamp"),
                    set.getField("tps"))).collect(Collectors.toList());
    }

    public void convertDeprecatedLogs() {
        if(logsDatabase instanceof FlatfileDatabase) {
            File oldFile = new File(Kauri.INSTANCE.getDataFolder().getPath() + File.separator + "logs.txt");

            if(oldFile.exists()) {
                ((FlatfileDatabase) logsDatabase)
                        .convertFromLegacy(oldFile);
                logsDatabase.getDatabaseValues().stream()
                        .filter(set -> !set.containsKey("type") && set.containsKey("vl"))
                        .forEach(set -> {
                            set.inputField("type", "log");
                            logsDatabase.updateObject(set);
                        });
                logsDatabase.saveDatabase();
            }
        }
    }
    
    public List<Punishment> getPunishments(UUID uuid) {
        List<StructureSet> structureSets = logsDatabase.getDatabaseValues()
                .stream()
                .filter(set -> set.containsKey("type") && set.getField("type").equals("punishment"))
                .collect(Collectors.toList());

        return structureSets.stream()
                .map(set ->
                        new Punishment(
                                uuid,
                                String.valueOf(set.getField("checkName")),
                                set.getField("timeStamp")))
                .collect(Collectors.toList());
    }

    public Map<UUID, List<Log>> getLogsWithinTimeFrame(long timeFrame) {
        long currentTime = System.currentTimeMillis();

        Map<UUID, List<Log>> logs = new HashMap<>();

        logsDatabase.getDatabaseValues()
                .stream()
                .filter(structSet -> structSet.containsKey("type") && structSet.getField("type").equals("log"))
                .filter(set -> {
                    long timeStamp = set.getField("timeStamp");

                    return (currentTime - timeStamp) < timeFrame;
                }).forEach(set -> {
            UUID uuid = UUID.fromString(set.getField("uuid"));
            List<Log> logList = logs.getOrDefault(uuid, new ArrayList<>());

            logList.add(new Log(
                    String.valueOf(set.getField("checkName")),
                    String.valueOf(set.getField("info")),
                    set.getField("vl"),
                    set.getField("ping"),
                    set.getField("timeStamp"),
                    set.getField("tps")));

            logs.put(uuid, logList);
        });

        logs.values().forEach(list -> list.sort(Comparator.comparing(log -> currentTime - log.timeStamp)));

        return logs;
    }

    private void save() {
        RunUtils.taskTimerAsync(() -> logsDatabase.saveDatabase(), 6000, 6000);
    }

}