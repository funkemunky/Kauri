package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.db.db.FlatfileDatabase;
import dev.brighten.db.db.StructureSet;
import dev.brighten.db.utils.Pair;
import dev.brighten.dev.depends.org.bson.Document;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class FlatfileStorage implements DataStorage {

    private FlatfileDatabase database;

    private List<Log> logs = new CopyOnWriteArrayList<>();
    private List<Punishment> punishments = new CopyOnWriteArrayList<>();

    public FlatfileStorage() {
        database = new FlatfileDatabase("logs");
        database.loadMappings();

        RunUtils.taskTimerAsync(() -> {
            if(logs.size() > 0) {
                for (Log log : logs) {
                    StructureSet set = database.create(UUID.randomUUID().toString());

                    Arrays.asList(new Pair<>("type", "log"),
                            new Pair<>("uuid", log.uuid.toString()),
                            new Pair<>("checkName", log.checkName),
                            new Pair<>("vl", log.vl),
                            new Pair<>("info", log.info),
                            new Pair<>("ping", log.ping),
                            new Pair<>("timeStamp", log.timeStamp),
                            new Pair<>("tps", log.tps)).forEach(pair -> set.input(pair.key, pair.value));

                    set.save(database);
                    logs.remove(log);
                }
            }
            if(punishments.size() > 0) {
                for(Punishment punishment : punishments) {
                    StructureSet set = database.create(UUID.randomUUID().toString());

                    Arrays.asList( new Pair<>("type", "punishment"),
                            new Pair<>("uuid", punishment.uuid.toString()),
                            new Pair<>("checkName", punishment.checkName),
                            new Pair<>("timeStamp", punishment.timeStamp))
                            .forEach(pair -> set.input(pair.key, pair.value));

                    set.save(database);
                    punishments.remove(punishment);
                }
            }
        }, Kauri.INSTANCE, 120L, 40L);
    }
    @Override
    public List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        List<StructureSet> sets = database.get(structSet ->
                structSet.contains("type") && structSet.getObject("type").equals("log")
                        && (long)structSet.getObject("timeStamp") >= timeFrom
                        && (long)structSet.getObject("timeStamp") <= timeTo
                        && (check == null || structSet.getObject("checkName").equals(check.name))
                        && structSet.contains("uuid") && structSet.getObject("uuid").equals(uuid.toString())).stream()
                .skip(arrayMin).limit(arrayMax).collect(Collectors.toList());

        List<Log> toReturn;
        if(sets.size() == 0) toReturn = new ArrayList<>();
        else {
            toReturn = sets.stream().map(set -> new Log(
                    uuid,
                    set.getObject("checkName"),
                    set.getObject("info"),
                    set.getObject("vl") instanceof Integer
                            ? (int)set.getObject("vl")
                            : (set.getObject("vl") instanceof Double
                            ? (float)(double)set.getObject("vl") : (float)set.getObject("vl")),
                    set.getObject("ping") instanceof Integer
                            ? (int)set.getObject("ping") : set.getObject("ping"),
                    (long)set.getObject("timeStamp"),
                    set.getObject("tps") instanceof Integer
                            ? (int)set.getObject("tps")
                            : set.getObject("tps") instanceof Double
                            ? (double)set.getObject("tps") : (float)set.getObject("tps")))
                    .collect(Collectors.toList());
        }
        return toReturn;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        List<StructureSet> structureSets = database
                .get(set -> set.contains("uuid") && set.getObject("uuid").equals(uuid.toString())
                        && (long)set.getObject("timeStamp") >= timeFrom
                        && (long)set.getObject("timeStamp") <= timeTo
                        && set.contains("type") && set.getObject("type").equals("punishment")).stream()
                .skip(arrayMin).limit(arrayMax).collect(Collectors.toList());

        return structureSets.stream()
                .map(set ->
                        new Punishment(
                                uuid,
                                set.getObject("checkName"),
                                set.getObject("timeStamp")))
                .collect(Collectors.toList());
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
        database.remove(set -> set.getObject("uuid").equals(uuid.toString()));
    }

    @Override
    public void addPunishment(Punishment punishment) {
        punishments.add(punishment);
    }
}
