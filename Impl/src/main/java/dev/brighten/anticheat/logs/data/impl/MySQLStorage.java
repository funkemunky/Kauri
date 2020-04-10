package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.sql.MySQL;
import dev.brighten.anticheat.logs.data.sql.Query;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MySQLStorage implements DataStorage {

    private List<Log> logs = new CopyOnWriteArrayList<>();
    private List<Punishment> punishments = new CopyOnWriteArrayList<>();

    public MySQLStorage() {
        MySQL.init();
        Query.prepare("CREATE TABLE IF NOT EXISTS `VIOLATIONS` (" +
                "`UUID` VARCHAR(64) NOT NULL," +
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
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("DROP INDEX `UUID` on `VIOLATIONS`");
            Query.prepare("CREATE INDEX `UUID` ON `VIOLATIONS` (UUID)").execute();
        });
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("DROP INDEX `UUID` ON `PUNISHMENTS`").execute();
            Query.prepare("CREATE INDEX `UUID` ON `PUNISHMENTS` (UUID)").execute();
        });
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Query.prepare("DROP INDEX `TIME` ON `VIOLATIONS`").execute();
            Query.prepare("CREATE INDEX `TIME` ON `VIOLATIONS` (`TIME`)").execute();
        });

        RunUtils.taskTimerAsync(() -> {
            if(logs.size() > 0) {
                for (Log log : logs) {
                    Query.prepare("INSERT INTO `VIOLATIONS`" +
                            " (`UUID`, `TIME`, `VL`, `CHECK`, `PING`, `TPS`, `INFO`) VALUES (?,?,?,?,?,?,?)")
                            .append(log.uuid.toString()).append(log.timeStamp).append(log.vl)
                            .append(log.checkName).append((int)log.ping).append(log.tps)
                            .append(log.info)
                            .execute();
                    logs.remove(log);
                }
            }
            if(punishments.size() > 0) {
                for(Punishment punishment : punishments) {
                    Query.prepare("INSERT INTO `PUNISHMENTS` (`UUID`,`TIME`,`CHECK`) VALUES (?,?,?)")
                            .append(punishment.uuid.toString())
                            .append(punishment.timeStamp).append(punishment.checkName)
                            .execute();
                    punishments.remove(punishment);
                }
            }
        }, Kauri.INSTANCE, 120L, 40L);
    }
    @Override
    public List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        List<Log> logs = new ArrayList<>();

        if(uuid != null) {
            Query.prepare("SELECT `TIME`, `VL`, `CHECK`, `PING`, `TPS`, `INFO` " +
                    "FROM `VIOLATIONS` WHERE `UUID` = ?"+ (check != null ? " AND WHERE `CHECK` = " + check.name : "")
                    + " AND `TIME` BETWEEN ? AND ? ORDER BY `TIME` DESC LIMIT ?,?")
                    .append(uuid.toString()).append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs ->
                            logs.add(new Log(uuid,
                                    rs.getString("CHECK"), rs.getString("INFO"),
                                    rs.getFloat("VL"), (long)rs.getInt("PING"),
                                    rs.getLong("TIME"), rs.getDouble("TPS"))));
        } else {
            Query.prepare("SELECT `UUID`, `TIME`, `VL`, `CHECK`, `PING`, `TPS`, `INFO` " +
                    "FROM `VIOLATIONS`" + (check != null ? " WHERE `CHECK` = " + check.name + " AND" : " WHERE")
                    + " `TIME` BETWEEN ? AND ? ORDER BY `TIME` DESC LIMIT ?,?")
                    .append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs -> {
                        logs.add(new Log(UUID.fromString(rs.getString("UUID")),
                                rs.getString("CHECK"), rs.getString("INFO"),
                                rs.getFloat("VL"), (long)rs.getInt("PING"),
                                rs.getLong("TIME"), rs.getDouble("TPS")));
                    });
        }

        return logs;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        List<Punishment> punishments = new ArrayList<>();

        if(uuid != null) {
            Query.prepare("SELECT `TIME`, `CHECK` FROM `PUNISHMENTS` " +
                    "WHERE `UUID` = ? AND TIME BETWEEN ? AND ? ORDER BY `TIME` DESC LIMIT ?,?")
                    .append(uuid.toString()).append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs -> punishments
                            .add(new Punishment(uuid, rs.getString("CHECK"), rs.getLong("TIME"))));
        } else {
            Query.prepare("SELECT `UUID`, `TIME`, `CHECK` FROM `PUNISHMENTS` " +
                    "WHERE TIME BETWEEN ? AND ? ORDER BY `TIME` DESC LIMIT ?,?")
                    .append(timeFrom).append(timeTo).append(arrayMin).append(arrayMax)
                    .execute(rs -> punishments
                            .add(new Punishment(UUID.fromString(rs.getString("UUID")),
                                    rs.getString("CHECK"), rs.getLong("TIME"))));
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
}
