package dev.brighten.anticheat.logs.data.impl;

import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NoStorage implements DataStorage {

    public NoStorage() {

    }


    @Override
    public void shutdown() {

    }
    @Override
    public List<Log> getLogs(UUID uuid) {
        return Collections.emptyList();
    }

    @Override
    public List<Log> getLogs(UUID uuid, int skip, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check) {
        return Collections.emptyList();
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check, int skip) {
        return Collections.emptyList();
    }

    @Override
    public List<Log> getLogs(UUID uuid, int skip, int limit, String... check) {
        return Collections.emptyList();
    }

    @Override
    public List<Log> getLogs(long beginningTime, long endTime) {
        return null;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid) {
        return Collections.emptyList();
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, long beginningTime, long endTime) {
        return Collections.emptyList();
    }

    @Override
    public void addLog(Log log) {

    }

    @Override
    public void removeAll(UUID uuid) {

    }

    @Override
    public void addPunishment(Punishment punishment) {

    }

    @Override
    public void cacheAPICall(UUID uuid, String name) {

    }

    @Override
    public UUID getUUIDFromName(String name) {
        return null;
    }

    @Override
    public String getNameFromUUID(UUID uuid) {
        return null;
    }
}
