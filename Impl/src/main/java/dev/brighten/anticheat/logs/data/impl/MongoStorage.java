package dev.brighten.anticheat.logs.data.impl;

import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.logs.data.DataStorage;
import dev.brighten.anticheat.logs.data.DatabaseParameters;
import dev.brighten.anticheat.logs.data.config.MongoConfig;
import dev.brighten.anticheat.logs.objects.Log;
import dev.brighten.anticheat.logs.objects.Punishment;
import dev.brighten.db.depends.com.mongodb.BasicDBObject;
import dev.brighten.db.depends.com.mongodb.MongoClientSettings;
import dev.brighten.db.depends.com.mongodb.MongoCredential;
import dev.brighten.db.depends.com.mongodb.ServerAddress;
import dev.brighten.db.depends.com.mongodb.client.*;
import dev.brighten.db.depends.com.mongodb.client.model.Aggregates;
import dev.brighten.db.depends.com.mongodb.client.model.Filters;
import dev.brighten.db.depends.com.mongodb.client.model.Indexes;
import dev.brighten.db.depends.com.mongodb.client.model.Updates;
import dev.brighten.dev.depends.org.bson.Document;
import dev.brighten.dev.depends.org.bson.conversions.Bson;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MongoStorage implements DataStorage {

    private MongoCollection<Document> logsCollection, punishmentsCollection, nameUUIDCollection, alertsCollection;
    private MongoDatabase database;
    private BukkitTask task;

    private Queue<Document> logs = new ConcurrentLinkedQueue<>(), punishments = new ConcurrentLinkedQueue<>();

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
        alertsCollection = database.getCollection("alertsStatus");

        MiscUtils.printToConsole("&7Creating indexes for logs...");
        logsCollection.createIndex(Indexes.ascending("uuid"));
        logsCollection.createIndex(Indexes.ascending("check"));
        logsCollection.createIndex(Indexes.ascending("vl"));
        MiscUtils.printToConsole("&aCompleted index creation!");
        MiscUtils.printToConsole("&7Creating index for punishments...");
        punishmentsCollection.createIndex(Indexes.ascending("uuid"));
        MiscUtils.printToConsole("&aCompleted index creation!");

        task = RunUtils.taskTimerAsync(() -> {
            Document doc = null;
            int count = 0;
            final List<Document> docsToInsert = new ArrayList<>();
            while((doc = logs.poll()) != null) {
                docsToInsert.add(doc);
                if(++count >= MongoConfig.batchInsertMax)
                    break;
            }

            if(count > 0) {
                logsCollection.insertMany(docsToInsert);
                docsToInsert.clear();
                count = 0;
                doc = null;
            }

            while((doc = punishments.poll()) != null) {
                docsToInsert.add(doc);
                if(++count >= MongoConfig.batchInsertMax)
                    break;
            }

            if(count > 0) {
                punishmentsCollection.insertMany(docsToInsert);
                docsToInsert.clear();
                count = 0;
                doc = null;
            }
        }, Kauri.INSTANCE, 120L, 20L);
    }
    @Override
    public List<Log> getLogs(UUID uuid, Check check, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        Bson document = new Document("$gte", timeFrom).append("$lt", timeTo);
        List<Document> logs = new ArrayList<>();

        List<Bson> aggregates = new ArrayList<>();

        if(uuid != null) aggregates.add(Aggregates.match(Filters.eq("uuid", uuid.toString())));
        if(check != null) aggregates.add(Aggregates.match(Filters.eq("check", check.name)));

        aggregates.addAll(Arrays.asList(Aggregates.match(Filters.eq("time", document)),
                new BasicDBObject("$sort", new BasicDBObject("time", -1))));

        if(arrayMin != 0 && arrayMax != Integer.MAX_VALUE) {
            aggregates.addAll(Arrays.asList(new BasicDBObject("$skip", arrayMin), new BasicDBObject("$limit", arrayMax)));
        }

        AggregateIterable<Document> agg = logsCollection.aggregate(aggregates).allowDiskUse(true);

        agg.forEach((Consumer<Document>) logs::add);

        return logs.stream()
                .map(doc -> new Log(UUID.fromString(doc.getString("uuid")), doc.getString("check"),
                        doc.getString("info"), doc.getDouble("vl").floatValue(), doc.getInteger("ping"),
                        doc.getLong("time"), doc.getDouble("tps")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Punishment> getPunishments(UUID uuid, int arrayMin, int arrayMax, long timeFrom, long timeTo) {
        Bson document = new Document("$gte", timeFrom).append("$lt", timeTo);
        List<Document> logs = new ArrayList<>();
        AggregateIterable<Document> agg = punishmentsCollection.aggregate(Arrays
                .asList(Aggregates.match(Filters.eq("uuid", uuid.toString())),
                        Aggregates.match(Filters.eq("time", document)),
                        new BasicDBObject("$skip", arrayMin), new BasicDBObject("$limit", arrayMax),
                        new BasicDBObject("$sort", new BasicDBObject("time", -1)))).allowDiskUse(true);

        agg.forEach((Consumer<Document>) logs::add);

        return logs.stream()
                .map(doc -> new Punishment(UUID.fromString(doc.getString("uuid")),
                        doc.getString("check"), doc.getLong("time")))
                .collect(Collectors.toList());
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
    public List<Log> getHighestVL(UUID uuid, Check check, int limit, long timeFrom, long timeTo) {
        Bson document = new Document("$gte", timeFrom).append("$lt", timeTo);
        List<Document> logs = new ArrayList<>();
        logsCollection.find(Filters.eq("uuid", uuid.toString()))
                .filter(new Document("time", document))
                .forEach((Consumer<Document>) logs::add);

        Map<String, Log> logsMax = new HashMap<>();

        logs.stream()
                .map(doc -> new Log(uuid, doc.getString("check"), doc.getString("info"),
                        doc.getDouble("vl").floatValue(), doc.getInteger("ping"), doc.getLong("time"),
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
        logs.add(new Document("uuid", log.uuid.toString())
                .append("time", log.timeStamp).append("check", log.checkName)
                .append("vl", (double)log.vl).append("info", log.info).append("ping", log.ping)
                .append("tps", log.tps));
    }

    @Override
    public void removeAll(UUID uuid) {
        punishmentsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
        logsCollection.deleteMany(Filters.eq("uuid", uuid.toString()));
    }

    @Override
    public void addPunishment(Punishment punishment) {
        punishments.add(new Document("uuid", punishment.uuid)
                .append("time", punishment.timeStamp).append("check", punishment.checkName));
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
        Document doc = nameUUIDCollection.find(Filters.eq("name", name)).first();

        if(doc != null) {
            return UUID.fromString(doc.getString("uuid"));
        }

        return null;
    }

    @Override
    public String getNameFromUUID(UUID uuid) {
        Document doc = nameUUIDCollection.find(Filters.eq("uuid", uuid.toString())).first();

        if(doc != null) {
            return doc.getString("name");
        }

        return null;
    }

    @Override
    public void updateAlerts(UUID uuid, boolean alertsEnabled) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Document doc = alertsCollection.find(Filters.eq("uuid", uuid.toString())).first();

            if(doc == null) {
                doc = new Document("uuid", uuid.toString());
                doc.put("normal", alertsEnabled);
                doc.put("dev", false);

                alertsCollection.insertOne(doc);
            } else {
                alertsCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                        Updates.set("normal", alertsEnabled));
            }
        });
    }

    @Override
    public void updateDevAlerts(UUID uuid, boolean devAlertsEnabled) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Document doc = alertsCollection.find(Filters.eq("uuid", uuid.toString())).first();

            if(doc == null) {
                doc = new Document("uuid", uuid.toString());
                doc.put("normal", false);
                doc.put("dev", devAlertsEnabled);

                alertsCollection.insertOne(doc);
            } else {
                alertsCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                        Updates.set("dev", devAlertsEnabled));
            }
        });
    }

    @Override
    public void alertsStatus(UUID uuid, Consumer<Boolean> result) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Document doc = alertsCollection.find(Filters.eq("uuid", uuid.toString())).first();

            if(doc != null) {
                result.accept(doc.getBoolean("normal"));
            } else result.accept(false);
        });
    }

    @Override
    public void devAlertsStatus(UUID uuid, Consumer<Boolean> result) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            Document doc = alertsCollection.find(Filters.eq("uuid", uuid.toString())).first();

            if(doc != null) {
                result.accept(doc.getBoolean("dev"));
            } else result.accept(false);
        });
    }
}
