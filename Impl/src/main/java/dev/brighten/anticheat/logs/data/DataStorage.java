package dev.brighten.anticheat.logs.data;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;

import java.util.List;
import java.util.UUID;

public interface DataStorage {

    List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo);

    List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo);

    List<Log> getHighestVL(UUID uuid, Check check, int limit, long timeFrom, long timeTo);

    void addLog(Log log);

    void removeAll(UUID uuid);

    void addPunishment(Punishment punishment);

    void cacheAPICall(UUID uuid, String name);

    UUID getUUIDFromName(String name);

    String getNameFromUUID(UUID uuid);
}
