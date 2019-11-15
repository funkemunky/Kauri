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
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Init
@NoArgsConstructor
public class LoggerManager {

    public Database logsDatabase;

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
                            MongoDatabase.initMongo(mongoDatabase, authDatabase, mongoIp, mongoPort,
                                    mongoUsername, mongoPassword));
                } else {
                    logsDatabase = new MongoDatabase("logs",
                            MongoDatabase.initMongo(mongoDatabase, mongoIp, mongoPort));
                }
            } else {
                MiscUtils.printToConsole("&7Setting up FlatfileDB...");
                logsDatabase = new FlatfileDatabase("logs");
            }
            MiscUtils.printToConsole("&7Loading database on second thread...");
            logsDatabase.loadDatabase();
            save();
        }
    }

    public void addLog(ObjectData data, Check check, String info) {
        Log log = new Log(check.name, info, check.vl, data.lagInfo.transPing,
                System.currentTimeMillis(), Kauri.INSTANCE.tps);

        StructureSet set = logsDatabase.createStructure(UUID.randomUUID().toString(),
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
        
        StructureSet set = logsDatabase.createStructure(UUID.randomUUID().toString(),
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

        if(Bukkit.getPluginManager().isPluginEnabled("KauriLoader")) {
            String license =
                    Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license");

            try {
                URL url = new URL("https://funkemunky.cc/download/verify?license="
                        + URLEncoder.encode(license, "UTF-8") + "&downloader=Kauri");

                try {
                    val connection = url.openConnection();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String line = reader.readLine();

                    boolean valid = Boolean.parseBoolean(line);

                    if(!valid) return null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else return null;

        return sets.stream().map(set -> new Log(
                set.getField("checkName"),
                set.getField("info"),
                set.getFloat("vl"),
                set.getField("ping"),
                set.getLong("timeStamp"),
                set.getDouble("tps")))
                .collect(Collectors.toList());
    }

    public void convertDeprecatedLogs() {

    }

    public void clearLogs(UUID uuid) {
        logsDatabase.getDatabaseValues()
                .stream()
                .filter(set -> set.getField("uuid").equals(uuid.toString()))
                .map(set -> set.id)
                .forEach(logsDatabase::remove);
    }
    
    public List<Punishment> getPunishments(UUID uuid) {
        List<StructureSet> structureSets = logsDatabase.getDatabaseValues()
                .stream()
                .filter(set -> set.getField("uuid") != null && set.getField("uuid").equals(uuid.toString())
                        && set.containsKey("type") && set.getField("type").equals("punishment"))
                .collect(Collectors.toList());

        return structureSets.stream()
                .map(set ->
                        new Punishment(
                                uuid,
                                set.getField("checkName"),
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
                    set.getField("checkName"),
                    set.getField("info"),
                    set.getFloat("vl"),
                    set.getField("ping"),
                    set.getLong("timeStamp"),
                    set.getDouble("tps")));

            logs.put(uuid, logList);
        });

        logs.values().forEach(list -> list.sort(Comparator.comparing(log -> currentTime - log.timeStamp)));

        return logs;
    }

    private void save() {
        RunUtils.taskTimerAsync(() -> logsDatabase.saveDatabase(), 2400, 2400);
    }

}