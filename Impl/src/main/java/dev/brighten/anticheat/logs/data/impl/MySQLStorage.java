package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.sql.ExecutableStatement;
import dev.brighten.anticheat.logs.data.sql.MySQL;
import dev.brighten.anticheat.logs.data.sql.Query;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import lombok.val;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MySQLStorage implements DataStorage {

    private List<Log> logs = new CopyOnWriteArrayList<>();
    private List<Punishment> punishments = new CopyOnWriteArrayList<>();
    private BukkitTask task;

    public MySQLStorage() {
        MySQL.init();
        Query.prepare("CREATE TABLE IF NOT EXISTS `VIOLATIONS` (" +
                "`UUID` VARCHAR(48) NOT NULL," +
                "`TIME` LONG NOT NULL," +
                "`VL` FLOAT NOT NULL," +
                "`CHECK` VARCHAR(32) NOT NULL," +
                "`PING` SMALLINT NOT NULL," +
                "`TPS` DOUBLE NOT NULL," +
                "`INFO` LONGTEXT NOT NULL)").execute();
        Query.prepare("CREATE TABLE IF NOT EXISTS `PUNISHMENTS` (" +
                "`UUID` VARCHAR(64) NOT NULL," +
                "`TIME` LONG NOT NULL," +
                "`CHECK` VARCHAR(32) NOT NULL)").execute();
        Query.prepare("CREATE TABLE IF NOT EXISTS `NAMECACHE` (" +
                "`UUID` VARCHAR(64) NOT NULL," +
                "`NAME` VARCHAR(16) NOT NULL," +
                "`TIMESTAMP` LONG NOT NULL)").execute();
        Kauri.INSTANCE.loggingThread.execute(() -> {
            MiscUtils.printToConsole("&7Creating UUID index for SQL violations...");
            Query.prepare("CREATE INDEX `UUID` ON `VIOLATIONS` (UUID)").execute();
            MiscUtils.printToConsole("&aCreated!");
            MiscUtils.printToConsole("&7Creating UUID index for SQL punishments...");
            Query.prepare("CREATE INDEX `UUID` ON `PUNISHMENTS` (UUID)").execute();
            MiscUtils.printToConsole("&aCreated!");
            MiscUtils.printToConsole("&a7 Creating TIME index for SQL violations...");
            Query.prepare("CREATE INDEX `TIME` ON `VIOLATIONS` (`TIME`)").execute();
            MiscUtils.printToConsole("&a7 Creating CHECK index for SQL violations...");
            Query.prepare("CREATE INDEX `CHECK` ON `VIOLATIONS` (`CHECK`)");
            MiscUtils.printToConsole("&aCreated!");
        });

        task = RunUtils.taskTimerAsync(() -> {
            if(logs.size() > 0) {
                for (Log log : logs) {
                    try {
                        Query.prepare("INSERT INTO `VIOLATIONS`" +
                                " (`UUID`, `TIME`, `VL`, `CHECK`, `PING`, `TPS`, `INFO`) VALUES (?,?,?,?,?,?,?)")
                                .append(log.uuid.toString()).append(log.timeStamp).append(log.vl)
                                .append(log.checkName).append((int)log.ping).append(log.tps)
                                .append(log.info)
                                .execute();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    logs.remove(log);
                }
            }
            if(punishments.size() > 0) {
                for(Punishment punishment : punishments) {
                    try {
                        Query.prepare("INSERT INTO `PUNISHMENTS` (`UUID`,`TIME`,`CHECK`) VALUES (?,?,?)")
                                .append(punishment.uuid.toString())
                                .append(punishment.timeStamp).append(punishment.checkName)
                                .execute();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    punishments.remove(punishment);
                }
            }
        }, Kauri.INSTANCE, 120L, 40L);
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
    public List<Log> getLogs(UUID uuid) {
        List<Log> logs = new ArrayList<>();

        Query.prepare("select * from `VIOLATIONS` where `UUID` = ?").append(uuid.toString()).
                execute(rs -> {
                    logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                            rs.getString("CHECK"), rs.getString("INFO"),
                            rs.getFloat("VL"), (long)rs.getInt("PING"),
                            rs.getLong("TIME"), rs.getDouble("TPS")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, int skip, int limit) {
        List<Log> logs = new ArrayList<>();

        Query.prepare("select * from `VIOLATIONS` where `UUID` = ? LIMIT ?,?")
                .append(uuid.toString()).append(skip).append(limit).
                execute(rs -> {
                    logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                            rs.getString("CHECK"), rs.getString("INFO"),
                            rs.getFloat("VL"), (long)rs.getInt("PING"),
                            rs.getLong("TIME"), rs.getDouble("TPS")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check) {
        List<Log> logs = new ArrayList<>();

        Query.prepare("select * from `VIOLATIONS` where `UUID` = ? and `CHECK` = ?")
                .append(uuid.toString()).append(check.name).
                execute(rs -> {
                    logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                            rs.getString("CHECK"), rs.getString("INFO"),
                            rs.getFloat("VL"), (long)rs.getInt("PING"),
                            rs.getLong("TIME"), rs.getDouble("TPS")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check, int limit) {
        List<Log> logs = new ArrayList<>();

        Query.prepare("select * from `VIOLATIONS` where `UUID` = ? and `CHECK` = ? LIMIT ?")
                .append(uuid.toString()).append(check.name).append(limit).
                execute(rs -> {
                    logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                            rs.getString("CHECK"), rs.getString("INFO"),
                            rs.getFloat("VL"), (long)rs.getInt("PING"),
                            rs.getLong("TIME"), rs.getDouble("TPS")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, int skip, int limit, String... checks) {
        List<Log> logs = new ArrayList<>();

        ExecutableStatement query = Query.prepare("select * from `VIOLATIONS` where `UUID` = ? and ("
                + Arrays.stream(checks).map(c -> "`CHECK` = ?").collect(Collectors.joining(" OR "))
                + ") LIMIT ?,?")
                .append(uuid.toString());

        for (String check : checks) {
            query = query.append(check);
        }

        query.append(skip).append(limit).
        execute(rs -> {
            logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                    rs.getString("CHECK"), rs.getString("INFO"),
                    rs.getFloat("VL"), (long)rs.getInt("PING"),
                    rs.getLong("TIME"), rs.getDouble("TPS")));
        });
        return logs;
    }

    @Override
    public List<Log> getLogs(long beginningTime, long endTime) {
        List<Log> logs = new ArrayList<>();

        Query.prepare("select * from `VIOLATIONS` where `TIME` between ? and ?")
                .append(beginningTime).append(endTime).
                execute(rs -> {
                    logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                            rs.getString("CHECK"), rs.getString("INFO"),
                            rs.getFloat("VL"), (long)rs.getInt("PING"),
                            rs.getLong("TIME"), rs.getDouble("TPS")));
                });
        return logs;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid) {
        List<Punishment> punishments = new ArrayList<>();

        Query.prepare("select `TIME`, `CHECK` from `PUNISHMENTS` where `UUID` = ?").append(uuid.toString())
                .execute(rs -> punishments
                        .add(new Punishment(uuid,
                                rs.getString("CHECK"), rs.getLong("TIME"))));
        return punishments;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, long beginningTime, long endTime) {
        List<Punishment> punishments = new ArrayList<>();

        Query.prepare("select `TIME`, `CHECK` from `PUNISHMENTS` where `UUID` = ? " +
                "and `TIME` between ? and ? order by `TIME` desc")
                .append(uuid.toString()).append(beginningTime).append(endTime)
                .execute(rs -> punishments
                        .add(new Punishment(uuid,
                                rs.getString("CHECK"), rs.getLong("TIME"))));
        return punishments;
    }

    @Override
    public void addLog(Log log) {
        logs.add(log);
    }

    @Override
    public void removeAll(UUID uuid) {
        Query.prepare("DELETE FROM `VIOLATIONS` WHERE UUID = ?").append(uuid.toString())
                .execute();
        Query.prepare("DELETE FROM `PUNISHMENTS` WHERE UUID = ?").append(uuid.toString())
                .execute();
    }

    @Override
    public void addPunishment(Punishment punishment) {
        punishments.add(punishment);
    }

    @Override
    public void cacheAPICall(UUID uuid, String name) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("DELETE FROM `NAMECACHE` WHERE `UUID` = ?").append(uuid.toString()).execute();
            Query.prepare("INSERT INTO `NAMECACHE` (`UUID`, `NAME`, `TIMESTAMP`) VALUES (?, ?, ?)")
                    .append(uuid.toString()).append(name).append(System.currentTimeMillis()).execute();
        });
    }

    @Override
    public UUID getUUIDFromName(String name) {
        try {
            val rs = Query.prepare("SELECT `UUID`, `TIMESTAMP` FROM `NAMECACHE` WHERE `NAME` = ?")
                    .append(name).executeQuery();

            String uuidString = rs.getString("UUID");

            if(uuidString != null) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));

                if(System.currentTimeMillis() - rs.getLong("TIMESTAMP") > TimeUnit.DAYS.toMillis(1)) {
                    Kauri.INSTANCE.loggingThread.execute(() -> {
                        Query.prepare("DELETE FROM `NAMECACHE` WHERE `UUID` = ?").append(uuidString).execute();
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
            val rs = Query.prepare("SELECT `NAME` `TIMESTAMP` FROM `NAMECACHE` WHERE `UUID` = ?")
                    .append(uuid.toString()).executeQuery();

            String name = rs.getString("NAME");

            if(name != null) {
                if(System.currentTimeMillis() - rs.getLong("TIMESTAMP") > TimeUnit.DAYS.toMillis(1)) {
                    Kauri.INSTANCE.loggingThread.execute(() -> {
                        Query.prepare("DELETE FROM `NAMECACHE` WHERE `NAME` = ?").append(name).execute();
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
}
