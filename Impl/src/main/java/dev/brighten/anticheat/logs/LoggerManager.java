package dev.brighten.anticheat.logs;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.db.db.*;
import dev.brighten.db.utils.Pair;
import lombok.NoArgsConstructor;
import lombok.val;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Init
@NoArgsConstructor
public class LoggerManager {

    public Database logsDatabase;
    private List<StructureSet> setsToSave = Collections.synchronizedList(new ArrayList<>());

    /*My SQL */
    @ConfigSetting(path = "database.mysql", name = "enabled")
    public static boolean mySQLEnabled = false;

    @ConfigSetting(path = "database.mysql", name = "username")
    public static String sqlUsername = "root";

    @ConfigSetting(path = "database.mysql", name = "database")
    public static String sqlDatabase = "Kauri";

    @ConfigSetting(path = "database.mysql", name = "password")
    public static String sqlPassword = "password";

    @ConfigSetting(path = "database.mysql", name = "ip")
    public static String sqlIp = "127.0.0.1";

    @ConfigSetting(path = "database.mysql", name = "port")
    public static int sqlPort = 3306;

    /* Mongo */
    @ConfigSetting(path = "database.mongo", name = "enabled")
    public static boolean mongoEnabled = false;

    @ConfigSetting(path = "database.mongo", name = "username")
    public static String mongoUsername = "root";

    @ConfigSetting(path = "database.mongo", name = "password")
    public static String mongoPassword = "password";

    @ConfigSetting(path = "database.mongo", name = "requiresLoginDetails")
    public static boolean mongoLoginDetails = false;

    @ConfigSetting(path = "database.mongo", name = "database")
    public static String mongoDatabase = "Kauri";

    @ConfigSetting(path = "database.mongo", name = "authDatabase")
    public static String authDatabase = "admin";

    @ConfigSetting(path = "database.mongo", name = "ip")
    public static String mongoIp = "127.0.0.1";

    @ConfigSetting(path = "database.mongo", name = "port")
    public static int mongoPort = 27017;

    @ConfigSetting(path = "database", name = "saveInterval")
    private static long saveTicks = 20 * 10L;

    public BukkitTask saveTask;

    public LoggerManager(boolean aLittleStupid) {
        if(aLittleStupid) {
            if(mySQLEnabled) {
                MiscUtils.printToConsole("&7Setting up SQL...");
                logsDatabase = new MySQLDatabase("logs");
                logsDatabase.connect(sqlIp, String.valueOf(sqlPort), sqlDatabase,
                        "true", sqlUsername, sqlPassword);
            } else if(mongoEnabled) {
                MiscUtils.printToConsole("&7Setting up Mongo...");
                logsDatabase = new MongoDatabase("logs");
                if(mongoLoginDetails) {
                    logsDatabase.connect(mongoIp, String.valueOf(mongoPort),
                            authDatabase, mongoUsername, mongoPassword, authDatabase);
                } else {
                    logsDatabase.connect(mongoIp, String.valueOf(mongoPort), mongoDatabase);
                }
            } else {
                MiscUtils.printToConsole("&7Setting up FlatfileDB...");
                logsDatabase = new FlatfileDatabase("logs");
            }
            MiscUtils.printToConsole("&7Loading database...");
            logsDatabase.loadMappings();
            runSaveTask();
        }
    }

    public void addLog(ObjectData data, Check check, String info) {
        Log log = new Log(check.name, info, check.vl, data.lagInfo.transPing,
                System.currentTimeMillis(), Kauri.INSTANCE.tps);

        StructureSet set = logsDatabase.create(UUID.randomUUID().toString());

        Arrays.asList(new Pair<>("type", "log"),
                new Pair<>("uuid", data.uuid.toString()),
                new Pair<>("checkName", log.checkName),
                new Pair<>("vl", log.vl),
                new Pair<>("info", log.info),
                new Pair<>("ping", log.ping),
                new Pair<>("timeStamp", log.timeStamp),
                new Pair<>("tps", log.tps)).forEach(pair -> set.input(pair.key, pair.value));

        setsToSave.add(set);
    }

    public void addPunishment(ObjectData data, Check check) {
        Punishment punishment = new Punishment(data.uuid, check.name, System.currentTimeMillis());

        StructureSet set = logsDatabase.create(UUID.randomUUID().toString());

        Arrays.asList( new Pair<>("type", "punishment"),
                new Pair<>("uuid", data.uuid.toString()),
                new Pair<>("checkName", punishment.checkName),
                new Pair<>("timeStamp", punishment.timeStamp))
                .forEach(pair -> set.input(pair.key, pair.value));
        
        set.save(logsDatabase);
        setsToSave.add(set);
    }

    public List<Log> getLogs(UUID uuid) {

        List<StructureSet> sets = logsDatabase.get(structSet ->
                structSet.contains("type") && structSet.getObject("type").equals("log")
                        && structSet.contains("uuid") && structSet.getObject("uuid").equals(uuid.toString()));

        if(sets.size() == 0) return new ArrayList<>();

        return sets.stream().map(set -> new Log(
                set.getObject("checkName"),
                set.getObject("info"),
                set.getObject("vl") instanceof Integer
                        ? (int)set.getObject("vl") : (float)(double)set.getObject("vl"),
                set.getObject("ping") instanceof Integer
                        ? (int)set.getObject("ping") : set.getObject("ping"),
                (long)set.getObject("timeStamp"),
                set.getObject("tps") instanceof Integer
                        ? (int)set.getObject("tps") : (double)set.getObject("tps")))
                .collect(Collectors.toList());
    }

    public void clearLogs(UUID uuid) {
        val array = logsDatabase.get(set -> !set.contains("uuid") || set.getObject("uuid").equals(uuid.toString()))
                .stream().map(StructureSet::getId).toArray(String[]::new);

        logsDatabase.remove(array);
    }
    
    public List<Punishment> getPunishments(UUID uuid) {
        List<StructureSet> structureSets = logsDatabase
                .get(set -> set.contains("uuid") && set.getObject("uuid").equals(uuid.toString())
                        && set.contains("type") && set.getObject("type").equals("punishment"));

        return structureSets.stream()
                .map(set ->
                        new Punishment(
                                uuid,
                                set.getObject("checkName"),
                                set.getObject("timeStamp")))
                .collect(Collectors.toList());
    }

    public Map<UUID, List<Log>> getLogsWithinTimeFrame(long timeFrame) {
        long currentTime = System.currentTimeMillis();

        Map<UUID, List<Log>> logs = new HashMap<>();

        logsDatabase.get(structSet -> {
            if(structSet.contains("type") && structSet.getObject("type").equals("log")
                    && structSet.contains("timeStamp")) {
                long timeStamp = structSet.getObject("timeStamp");

                return (currentTime - timeStamp) < timeFrame;
            } else return false;
        }).forEach(set -> {
            UUID uuid = UUID.fromString(set.getObject("uuid"));
            List<Log> logList = logs.getOrDefault(uuid, new ArrayList<>());

            logList.add(new Log(
                    set.getObject("checkName"),
                    set.getObject("info"),
                    set.getObject("vl") instanceof Integer
                            ? (int)set.getObject("vl") : (float)(double)set.getObject("vl"),
                    set.getObject("ping") instanceof Integer
                            ? (int)set.getObject("ping") : set.getObject("ping"),
                    (long)set.getObject("timeStamp"),
                    set.getObject("tps") instanceof Integer
                            ? (int)set.getObject("tps") : (double)set.getObject("tps")));

            logs.put(uuid, logList);
        });

        logs.values().forEach(list -> list.sort(Comparator.comparing(log -> currentTime - log.timeStamp)));

        return logs;
    }

    public void save() {
        if(logsDatabase != null && setsToSave.size() > 0) {
            for (StructureSet set : setsToSave) {
                set.save(logsDatabase);
                setsToSave.remove(set);
            }
        }
    }

    private void runSaveTask() {
        //If already created and running, don't make another one.
        if(saveTask != null && !saveTask.isCancelled()) return;
        saveTask = RunUtils.taskTimerAsync(this::save, Kauri.INSTANCE, 120L, saveTicks);
    }

}