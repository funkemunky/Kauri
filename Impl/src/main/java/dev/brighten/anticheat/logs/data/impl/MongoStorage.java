package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.config.MongoConfig;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.db.depends.com.mongodb.*;
import dev.brighten.db.depends.com.mongodb.client.*;
import dev.brighten.db.depends.com.mongodb.client.MongoClient;
import dev.brighten.db.depends.com.mongodb.client.model.Aggregates;
import dev.brighten.db.depends.com.mongodb.client.model.Filters;
import dev.brighten.db.depends.com.mongodb.client.model.Indexes;
import dev.brighten.db.depends.com.mongodb.client.model.Sorts;
import dev.brighten.dev.depends.org.bson.Document;
import dev.brighten.dev.depends.org.bson.conversions.Bson;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MongoStorage implements DataStorage {

    private MongoCollection<Document> logsCollection, punishmentsCollection, nameUUIDCollection;
    private MongoDatabase database;
    private BukkitTask task;

    private List<Log> logs = new CopyOnWriteArrayList<>();
    private List<Punishment> punishments = new CopyOnWriteArrayList<>();

    public MongoStorage() {
        MongoClient client;
        if(MongoConfig.loginDetails) {
            client = MongoClients.create(MongoClientSettings.builder().applyToClusterSettings(builder ->
                    builder.hosts(Collections.singletonList(new ServerAddress(MongoConfig.ip, MongoConfig.port))))
                    .credential(MongoCredential.createCredential(MongoConfig.username,
                            MongoConfig.authDatabase.length() > 0 ? MongoConfig.authDatabase : MongoConfig.database,
                            MongoConfig.password.toCharArray()))
                    .build());
        } else {
            client = MongoClients.create(MongoClientSettings.builder().applyToClusterSettings(builder ->
                    builder.hosts(Collections.singletonList(new ServerAddress(MongoConfig.ip, MongoConfig.port))))
                    .build());
        }
        database = client.getDatabase(MongoConfig.database);
        logsCollection = database.getCollection("logs");
        punishmentsCollection = database.getCollection("punishments");
        nameUUIDCollection = database.getCollection("nameUuid");

        MiscUtils.printToConsole("&7Creating indexes for logs...");
        logsCollection.createIndex(Indexes.ascending("uuid"));
        logsCollection.createIndex(Indexes.ascending("check"));
        logsCollection.createIndex(Indexes.ascending("vl"));
        MiscUtils.printToConsole("&aCompleted index creation!");
        MiscUtils.printToConsole("&7Creating index for punishments...");
        punishmentsCollection.createIndex(Indexes.ascending("uuid"));
        MiscUtils.printToConsole("&aCompleted index creation!");

        task = RunUtils.taskTimerAsync(() -> {
            if(logs.size() > 0) {
                for (Log log : logs) {
                    logsCollection.insertOne(new Document("uuid", log.uuid.toString())
                            .append("time", log.timeStamp).append("check", log.checkName)
                            .append("vl", (double)log.vl).append("info", log.info).append("ping", log.ping)
                            .append("tps", log.tps));
                    logs.remove(log);
                }
            }
            if(punishments.size() > 0) {
                for(Punishment punishment : punishments) {
                    punishmentsCollection.insertOne(new Document("uuid", punishment.uuid)
                            .append("time", punishment.timeStamp).append("check", punishment.checkName));
                    punishments.remove(punishment);
                }
            }
        }, Kauri.INSTANCE, 120L, 40L);
    }

    @Override
    public List<Log> getLogs(UUID uuid) {
        List<Log> logs = new ArrayList<>();
        logsCollection.find(Filters.eq("uuid", uuid.toString())).sort(Sorts.descending("time"))
                .forEach((Consumer<? super Document>) doc -> {
                    logs.add(new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                            doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getLong("ping"),
                            doc.getLong("time"), doc.getDouble("tps")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, int skip, int limit) {
        List<Log> logs = new ArrayList<>();
        logsCollection.find(Filters.eq("uuid", uuid.toString()))
                .sort(Sorts.descending("time"))
                .skip(skip)
                .limit(limit)
                .forEach((Consumer<? super Document>) doc -> {
                    logs.add(new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                            doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getLong("ping"),
                            doc.getLong("time"), doc.getDouble("tps")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check) {
        List<Log> logs = new ArrayList<>();
        logsCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()),
                Filters.eq("check", check.name)))
                .sort(Sorts.descending("time"))
                .forEach((Consumer<? super Document>) doc -> {
                    logs.add(new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                            doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getLong("ping"),
                            doc.getLong("time"), doc.getDouble("tps")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, Check check, int limit) {
        List<Log> logs = new ArrayList<>();
        logsCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()),
                Filters.eq("check", check.name)))
                .sort(Sorts.descending("time"))
                .limit(limit)
                .forEach((Consumer<? super Document>) doc -> {
                    logs.add(new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                            doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getLong("ping"),
                            doc.getLong("time"), doc.getDouble("tps")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(UUID uuid, int skip, int limit, String... check) {
        List<Log> logs = new ArrayList<>();
        Bson[] checkfilters = Arrays.stream(check)
                .map(c -> Filters.eq("check", check))
                .toArray(Bson[]::new);
        logsCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()),
                Filters.or(checkfilters)))
                .sort(Sorts.descending("time"))
                .skip(skip).limit(limit)
                .forEach((Consumer<? super Document>) doc -> {
                    logs.add(new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                            doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getLong("ping"),
                            doc.getLong("time"), doc.getDouble("tps")));
                });
        return logs;
    }

    @Override
    public List<Log> getLogs(long beginningTime, long endTime) {
        List<Log> logs = new ArrayList<>();
        logsCollection.find(Filters.and(Filters.gte("time", beginningTime),
                Filters.lte("time", endTime)))
                .sort(Sorts.descending("time"))
                .forEach((Consumer<? super Document>) doc -> {
                    logs.add(new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                            doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getLong("ping"),
                            doc.getLong("time"), doc.getDouble("tps")));
                });
        return logs;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid) {
        List<Punishment> punishments = new ArrayList<>();

        punishmentsCollection.find(Filters.eq("uuid", uuid.toString()))
                .sort(Sorts.descending("time"))
                .forEach((Block<? super Document>) doc -> {
                    punishments.add(new Punishment(UUID.fromString(doc.getString("uuid")),
                            doc.getString("check"), doc.getLong("time")));
                });
        return punishments;
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, long beginningTime, long endTime) {
        List<Punishment> punishments = new ArrayList<>();

        punishmentsCollection.find(Filters.and(Filters.eq("uuid", uuid.toString()),
                Filters.gte("time", beginningTime),
                Filters.lte("time", endTime)))
                .sort(Sorts.descending("time"))
                .forEach((Block<? super Document>) doc -> {
                    punishments.add(new Punishment(UUID.fromString(doc.getString("uuid")),
                            doc.getString("check"), doc.getLong("time")));
                });
        return punishments;
    }


    @Override
    public void shutdown() {
        task.cancel();
        task = null;
        logs.clear();
        punishments.clear();
        database = null;
        logsCollection = null;
        punishmentsCollection = null;
        nameUUIDCollection = null;
    }

    @Override
    public void addLog(Log log) {
        logs.add(log);
    }

    @Override
    public void removeAll(UUID uuid) {
        punishmentsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
        logsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
    }

    @Override
    public void addPunishment(Punishment punishment) {
        punishments.add(punishment);
    }

    @Override
    public void cacheAPICall(UUID uuid, String name) {
        nameUUIDCollection.deleteMany(Filters.or(Filters.eq("uuid", uuid.toString()),
                Filters.eq("name", name)));

        Document document = new Document("uuid", uuid.toString());
        document.put("name", name);
        document.put("timestamp", System.currentTimeMillis());

        nameUUIDCollection.insertOne(document);
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
