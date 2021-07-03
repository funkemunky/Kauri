package dev.brighten.anticheat.logs.data;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;

import java.util.List;
import java.util.UUID;

public interface DataStorage {

    List<Log> getLogs(UUID uuid);

    List<Log> getLogs(UUID uuid, int skip, int limit);

    List<Log> getLogs(UUID uuid, Check check);

    List<Log> getLogs(UUID uuid, Check check, int limit);

    List<Log> getLogs(UUID uuid, int skip, int limit, String... check);

    List<Log> getLogs(long beginningTime, long endTime);

    List<Punishment> getPunishments(UUID uuid);

    List<Punishment> getPunishments(UUID uuid, long beginningTime, long endTime);



    void addLog(Log log);

    void removeAll(UUID uuid);

    void addPunishment(Punishment punishment);

    void cacheAPICall(UUID uuid, String name);

    UUID getUUIDFromName(String name);

    String getNameFromUUID(UUID uuid);

    void shutdown();
}
