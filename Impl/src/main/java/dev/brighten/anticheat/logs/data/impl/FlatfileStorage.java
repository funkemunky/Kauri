package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.DatabaseParameters;
import dev.brighten.anticheat.logs.data.config.MySQLConfig;
import dev.brighten.anticheat.logs.data.sql.ExecutableStatement;
import dev.brighten.anticheat.logs.data.sql.MySQL;
import dev.brighten.anticheat.logs.data.sql.Query;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import lombok.val;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FlatfileStorage implements DataStorage {

    private final Deque<Log> logs = new LinkedList<>();
    private final Deque<Punishment> punishments = new LinkedList<>();
    private BukkitTask task;

    public FlatfileStorage() {
        MySQL.initSqlLite();
        Query.prepare("create table if not exists `violations` (" +
                "`uuid` varchar(36) not null," +
                "`time` timestamp not null," +
                "`VL` float not null," +
                "`check` varchar(32) not null," +
                "`ping` smallint not null," +
                "`tps` double not null," +
                "`info` text not null)").execute();
        Query.prepare("create table if not exists `punishments` (" +
                "`uuid` varchar(36) not null," +
                "`time` long not null," +
                "`check` varchar(32) not null)").execute();
        Query.prepare("create table if not exists `namecache` (" +
                "`uuid` varchar(32) not null," +
                "`name` varchar(16) not null," +
                "`timestamp` timestamp not null)").execute();

        Query.prepare("create table if not exists `alerts` (`uuid` varchar(36) unique)").execute();
        Query.prepare("create table if not exists `dev_alerts` (`uuid` varchar(36) unique)").execute();

        Kauri.INSTANCE.loggingThread.execute(() -> {
            MiscUtils.printToConsole("&7Creating UUID index for SQL violations...");
            Query.prepare("create index if not exists `UUID_1`ON `violations` (UUID)").execute();
            MiscUtils.printToConsole("&7Creating CHECK index for SQL violations...");
            Query.prepare("create index if not exists `CHECK_1` ON `violations` (CHECK)").execute();
            MiscUtils.printToConsole("&7Creating TIME index for SQL violations...");
            Query.prepare("create index if not exists `TIME_1` ON `violations` (TIME)`").execute();
            MiscUtils.printToConsole("&7Creating VL index for SQL violations...");
            Query.prepare("create index if not exists `VL_1` on `violations` (VL)");
            MiscUtils.printToConsole("&aCreated!");
            MiscUtils.printToConsole("&7Creating UUID index for SQL punishments...");
            Query.prepare("create index if not exists `UUID_2` ON `punishments` (UUID)").execute();
            MiscUtils.printToConsole("&aCreated!");
            MiscUtils.printToConsole("&a7 Creating TIME index for SQL punishments...");
            Query.prepare("create index if not exists `TIME_1` ON `punishments` (`time`)").execute();
            MiscUtils.printToConsole("&a7 Creating UUID index for SQL namecache...");
            Query.prepare("create index if not exists `UUID_1` ON `namecache` (`uuid`)");
            MiscUtils.printToConsole("&aCreated!");
        });

        RunUtils.taskTimerAsync(() -> {
            if(logs.size() > 0) {
                synchronized (logs) {
                    final StringBuilder values = new StringBuilder();

                    List<Object> objectsToInsert = new ArrayList<>();
                    Log log = null;
                    int amount = 0;
                    while((log = logs.poll()) != null) {
                        objectsToInsert.add(log.uuid.toString());
                        objectsToInsert.add(new Timestamp(log.timeStamp));
                        objectsToInsert.add(log.vl);
                        objectsToInsert.add(log.checkName);
                        objectsToInsert.add((int)log.ping);
                        objectsToInsert.add(log.tps);
                        objectsToInsert.add(log.info);

                        if(++amount >= 150) break;
                    }

                    for (int i = 0; i < amount; i++) {
                        values.append(i > 0 ? "," : "").append("(?, ?, ?, ?, ?, ?, ?)");
                    }

                    ExecutableStatement statement = Query.prepare("insert into `violations` " +
                            "(`uuid`, `time`, `VL`, `check`, `ping`, `tps`, `info`) values" + values.toString())
                            .append(objectsToInsert.toArray());


                    if(MySQLConfig.debugMessages)
                        Kauri.INSTANCE.getLogger().log(Level.INFO, "Inserted " + amount
                                + " logs into the database.");

                    statement.execute();

                    objectsToInsert.clear();
                }
            }
            if(punishments.size() > 0) {
                synchronized (punishments) {
                    final StringBuilder values = new StringBuilder();

                    List<Object> objectsToInsert = new ArrayList<>();
                    Punishment punishment = null;
                    int amount = 0;
                    while((punishment = punishments.poll()) != null) {
                        objectsToInsert.add(punishment.uuid.toString());
                        objectsToInsert.add(new Timestamp(punishment.timeStamp));
                        objectsToInsert.add(punishment.checkName);

                        if(++amount >= 150) break;
                    }

                    for (int i = 0; i < amount; i++) {
                        values.append(i > 0 ? "," : "").append("(?, ?, ?)");
                    }

                    ExecutableStatement statement = Query.prepare("insert into `punishments` " +
                            "(`uuid`,`time`,`check`) values " + values)
                            .append(objectsToInsert.toArray());
                    
                    if(MySQLConfig.debugMessages)
                        Kauri.INSTANCE.getLogger().log(Level.INFO, "Inserted " + amount
                                + " punishments into the database.");

                    statement.execute();
                }
            }
        }, Kauri.INSTANCE, 120L, 20L * MySQLConfig.rateInSeconds);
    }

    @Override
    public void shutdown() {
        task.cancel();
        task = null;
        logs.clear();
        punishments.clear();
        MySQL.shutdown();
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        List<Log> logs = new ArrayList<>();

        if(uuid != null) {
            Query.prepare("select `time`, `VL`, `check`, `ping`, `tps`, `info` " +
                    "from `violations` where `uuid` = ?"+ (check != null ? " and where `check` = " + check.name : "")
                    + " and `time` between ? and ? order by `time` desc limit ?,?")
                    .append(uuid.toString()).append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs ->
                            logs.add(new Log(uuid,
                                    rs.getString("check"), rs.getString("info"),
                                    rs.getFloat("vl"), rs.getInt("ping"),
                                    rs.getLong("time"), rs.getDouble("tps"))));
        } else {
            Query.prepare("select `uuid`, `time`, `VL`, `check`, `ping`, `tps`, `info` " +
                    "from `violations`" + (check != null ? " where `check` = " + check.name + " and" : " where")
                    + " `time` between ? and ? order by `time` desc limit ?,?")
                    .append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs -> {
                        logs.add(new Log(UUID.fromString(rs.getString("uuid")),
                                rs.getString("check"), rs.getString("info"),
                                rs.getFloat("vl"), rs.getInt("ping"),
                                rs.getLong("time"), rs.getDouble("tps")));
                    });
        }

        return logs;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        List<Punishment> punishments = new ArrayList<>();

        if(uuid != null) {
            Query.prepare("select `time`, `check` from `punishments` " +
                    "where `uuid` = ? and TIME between ? and ? order by `time` desc limit ?,?")
                    .append(uuid.toString()).append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs -> punishments
                            .add(new Punishment(uuid, rs.getString("check"), rs.getLong("time"))));
        } else {
            Query.prepare("select `uuid`, `time`, `check` from `punishments` " +
                    "where TIME between ? and ? order by `time` desc limit ?,?")
                    .append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs -> punishments
                            .add(new Punishment(UUID.fromString(rs.getString("uuid")),
                                    rs.getString("check"), rs.getLong("time"))));
        }

        return punishments;
    }

    @Override
    public List<Log> getHighestVL(UUID uuid, Check check, int limit, long timeFrom, long timeTo) {
        List<Log> logs = getLogs(uuid, check, 0, limit, timeFrom, timeTo);

        Map<String, Log> logsMax = new HashMap<>();

        logs.forEach(log -> {
            if(logsMax.containsKey(log.checkName)) {
                Log toCheck = logsMax.get(log.checkName);

                if(toCheck.vl < log.vl) {
                    logsMax.put(log.checkName, log);
                }
            } else logsMax.put(log.checkName, log);
        });
        return new ArrayList<>(logsMax.values());
    }

    @Override
    public List<Log> getLogs(UUID uuid, DatabaseParameters params) {
        List<Log> logs = new ArrayList<>();
        Query.prepare("select * from `violations` where `uuid` = ?"
                + (params.getTimeFrom() != -1 && params.getTimeTo() != -1 ? " and `time` between "
                + params.getTimeFrom() + " and " + params.getTimeTo()
                : (params.getTimeFrom() != -1
                ? " and `time` >= " + params.getTimeFrom() : "")
                + (params.getTimeTo() != -1
                ? " and `time` <= " + params.getTimeTo() : ""))
                + (params.getSkip() != -1 || params.getLimit() != -1 ? " order by `uuid` desc" : "")
                + (params.getSkip() != -1 ? " skip " + params.getSkip() : "")
                + (params.getLimit() != -1 ? " limit " + params.getLimit() : ""))
                .append(uuid.toString()).execute(rs ->
                logs.add(new Log(uuid,
                        rs.getString("check"), rs.getString("info"),
                        rs.getFloat("vl"), rs.getInt("ping"),
                        rs.getLong("time"), rs.getDouble("tps"))));
        return logs;
    }

    @Override
    public List<Log> getLogs(Check check, DatabaseParameters params) {
        List<Log> logs = new ArrayList<>();
        Query.prepare("select * from `violations` where `check` = ?"
                + (params.getTimeFrom() != -1 && params.getTimeTo() != -1 ? " and `time` between "
                + params.getTimeFrom() + " and " + params.getTimeTo()
                : (params.getTimeFrom() != -1
                ? " and `time` >= " + params.getTimeFrom() : "")
                + (params.getTimeTo() != -1
                ? " and `time` <= " + params.getTimeTo() : ""))
                + (params.getSkip() != -1 || params.getLimit() != -1 ? " order by `uuid` desc" : "")
                + (params.getSkip() != -1 ? " skip " + params.getSkip() : "")
                + (params.getLimit() != -1 ? " limit " + params.getLimit() : ""))
                .append(check.name)
                .execute(rs ->
                        logs.add(new Log(UUID.fromString(rs.getString("uuid")),
                                rs.getString("check"), rs.getString("info"),
                                rs.getFloat("vl"), rs.getInt("ping"),
                                rs.getLong("time"), rs.getDouble("tps"))));
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check, DatabaseParameters params) {
        List<Log> logs = new ArrayList<>();
        Query.prepare("select * from `violations` where `uuid` = ? and `check` = ?"
                + (params.getTimeFrom() != -1 && params.getTimeTo() != -1 ? " and `time` between "
                    + params.getTimeFrom() + " and " + params.getTimeTo()
                : (params.getTimeFrom() != -1
                    ? " and `time` >= " + params.getTimeFrom() : "")
                + (params.getTimeTo() != -1
                    ? " and `time` <= " + params.getTimeTo() : "")) 
                + (params.getSkip() != -1 || params.getLimit() != -1 ? " order by `uuid` desc" : "")
                + (params.getSkip() != -1 ? " skip " + params.getSkip() : "") 
                + (params.getLimit() != -1 ? " limit " + params.getLimit() : ""))
                .append(uuid.toString()).append(check.name)
                .execute(rs ->
                        logs.add(new Log(uuid,
                                rs.getString("check"), rs.getString("info"),
                                rs.getFloat("vl"), rs.getInt("ping"),
                                rs.getLong("time"), rs.getDouble("tps"))));
        return logs;
    }

    @Override
    public List<Log> getHighestVL(UUID uuid, Check check, DatabaseParameters databaseParameters) {
        List<Log> logsToSort = null;

        if(uuid != null && check != null) {
            logsToSort = getLogs(uuid, check, databaseParameters);
        } else if(uuid != null) {
            logsToSort = getLogs(uuid, databaseParameters);
        }

        Map<String, Log> logsMax = new HashMap<>();

        if(logsToSort == null) return Collections.emptyList();

        for (Log log : logsToSort) {
            logsMax.compute(log.checkName, (key, value) -> {
                if(value == null) {
                    return log;
                }

                if(log.vl > value.vl)
                    return log;

                return value;
            });
        }
        return new ArrayList<>(logsMax.values());
    }

    @Override
    public void addLog(Log log) {
        synchronized (logs) {
            logs.add(log);
        }
    }

    @Override
    public void removeAll(UUID uuid) {
        Query.prepare("delete from `violations` where UUID = ?").append(uuid.toString())
                .execute();
        Query.prepare("delete from `punishments` where UUID = ?").append(uuid.toString())
                .execute();
    }

    @Override
    public void addPunishment(Punishment punishment) {
        synchronized (punishments) {
            punishments.add(punishment);
        }
    }

    @Override
    public void cacheAPICall(UUID uuid, String name) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("delete from `namecache` where `uuid` = ?").append(uuid.toString()).execute();
            Query.prepare("insert into `namecache` (`uuid`, `name`, `timestamp`) values (?, ?, ?)")
                    .append(uuid.toString()).append(name).append(System.currentTimeMillis()).execute();
        });
    }

    @Override
    public UUID getUUIDFromName(String name) {
        try {
            val rs = Query.prepare("select `uuid`, `timestamp` from `namecache` where `name` = ?")
                    .append(name).executeQuery();

            String uuidString = rs.getString("uuid");

            if(uuidString != null) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));

                if(System.currentTimeMillis() - rs.getLong("timestamp") > TimeUnit.DAYS.toMillis(1)) {
                    Kauri.INSTANCE.loggingThread.execute(() -> {
                        Query.prepare("delete from `namecache` where `uuid` = ?").append(uuidString).execute();
                        MiscUtils.printToConsole("Deleted " + uuidString + " from name cache (age > 1 day).");
                    });
                }
                return uuid;
            }
        } catch (SQLException e) {
            RunUtils.task(e::printStackTrace);
        } catch(Exception e) {
            e.printStackTrace();
            //Empty catch
        }
        return null;
    }

    @Override
    public String getNameFromUUID(UUID uuid) {
        try {
            val rs = Query.prepare("select `name` `timestamp` from `namecache` where `uuid` = ?")
                    .append(uuid.toString()).executeQuery();

            String name = rs.getString("name");

            if(name != null) {
                if(System.currentTimeMillis() - rs.getLong("timestamp") > TimeUnit.DAYS.toMillis(1)) {
                    Kauri.INSTANCE.loggingThread.execute(() -> {
                        Query.prepare("delete from `namecache` where `name` = ?").append(name).execute();
                        MiscUtils.printToConsole("Deleted " + name + " from name cache (age > 1 day).");
                    });
                }
                return name;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateAlerts(UUID uuid, boolean alertsEnabled) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            if(alertsEnabled) {
                Query.prepare("insert into `alerts` (`uuid`) ? ON DUPLICATE KEY UPDATE")
                        .append(uuid.toString()).execute();
            } else Query.prepare("delete from `alerts` where `uuid` = ?").append(uuid.toString()).execute();
        });
    }

    @Override
    public void updateDevAlerts(UUID uuid, boolean devAlertsEnabled) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            if(devAlertsEnabled) {
                Query.prepare("insert into `dev_alerts` (`uuid`) ? ON DUPLICATE KEY UPDATE")
                        .append(uuid.toString()).execute();
            } else Query.prepare("delete from `dev_alerts` where `uuid` = ?").append(uuid.toString()).execute();
        });
    }

    @Override
    public void alertsStatus(UUID uuid, Consumer<Boolean> result) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("select * from `alerts` where `uuid` = ?").append(uuid.toString())
                    .executeSingle(rs -> result.accept(rs != null));
        });
    }

    @Override
    public void devAlertsStatus(UUID uuid, Consumer<Boolean> result) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("select * from `dev_alerts` where `uuid` = ?").append(uuid.toString())
                    .executeSingle(rs -> result.accept(rs != null));
        });
    }
}
