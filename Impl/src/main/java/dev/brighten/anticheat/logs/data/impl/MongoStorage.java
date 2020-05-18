package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.config.MongoConfig;
import dev.brighten.anticheat.logs.data.sql.Query;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.db.depends.com.mongodb.MongoClientSettings;
import dev.brighten.db.depends.com.mongodb.MongoCredential;
import dev.brighten.db.depends.com.mongodb.ServerAddress;
import dev.brighten.db.depends.com.mongodb.client.MongoClient;
import dev.brighten.db.depends.com.mongodb.client.MongoClients;
import dev.brighten.db.depends.com.mongodb.client.MongoCollection;
import dev.brighten.db.depends.com.mongodb.client.MongoDatabase;
import dev.brighten.db.depends.com.mongodb.client.model.Filters;
import dev.brighten.dev.depends.org.bson.Document;
import dev.brighten.dev.depends.org.bson.conversions.Bson;
import lombok.val;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MongoStorage implements DataStorage {

    private MongoCollection<Document> logsCollection, punishmentsCollection;
    private MongoDatabase database;

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

        RunUtils.taskTimerAsync(() -> {
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
    public List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        Bson document = new Document("$gte", timeFrom).append("$lt", timeTo);
        List<Document> logs = new ArrayList<>();
        val iterable = logsCollection.find(Filters.eq("uuid", uuid.toString()));

        if(check == null) {
            iterable.filter(new Document("time", document))
                    .skip(arrayMin).limit(arrayMax).sort(new Document("time", -1))
                    .forEach((Consumer<Document>) logs::add);
        } else {
            iterable.filter(Filters.eq("check", check.name));
            iterable.filter(new Document("time", document));
            iterable.skip(arrayMin);
            iterable.limit(arrayMax);
            iterable.sort(new Document("time", -1));
            iterable.forEach((Consumer<Document>) logs::add);
        }

        return logs.stream()
                .map(doc -> new Log(uuid, doc.getString("check"), doc.getString("info"),
                doc.getDouble("vl").floatValue(), doc.getLong("ping"), doc.getLong("time"),
                doc.getDouble("tps"))).collect(Collectors.toList());
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        Bson document = new Document("$gte", timeFrom).append("$lt", timeTo);
        List<Document> logs = new ArrayList<>();
        punishmentsCollection.find(Filters.eq("uuid", uuid.toString()))
                .filter(new Document("time", document))
                .skip(arrayMin).limit(arrayMax).sort(new Document("time", -1))
                .forEach((Consumer<Document>) logs::add);

        return logs.stream()
                .map(doc -> new Punishment(uuid, doc.getString("check"), doc.getLong("time")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Log> getHighestVL(UUID uuid, Check check, int limit, long timeFrom, long timeTo) {
        Bson document = new Document("$gte", timeFrom).append("$lt", timeTo);
        List<Document> logs = new ArrayList<>();
        logsCollection.find(Filters.eq("uuid", uuid.toString()))
                .filter(new Document("time", document))
                .forEach((Consumer<Document>) logs::add);

        Map<String, Log> logsMax = new HashMap<>();

        logs.stream()
                .map(doc -> new Log(uuid, doc.getString("check"), doc.getString("info"),
                        doc.getDouble("vl").floatValue(), doc.getLong("ping"), doc.getLong("time"),
                        doc.getDouble("tps")))
                .forEach(log -> {
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
        punishmentsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
        logsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
    }

    @Override
    public void addPunishment(Punishment punishment) {
       punishments.add(punishment);
    }
}
