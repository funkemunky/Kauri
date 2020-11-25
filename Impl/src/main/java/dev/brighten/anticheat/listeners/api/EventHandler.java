package dev.brighten.anticheat.listeners.api;

import cc.funkemunky.api.utils.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EventHandler {

    private final Map<String, Consumer> listeners = Collections.synchronizedMap(new HashMap<>());
    private final Map<Class<?>, List<String>> listenersCache = new ConcurrentHashMap<>();

    public <T> String listen(Class<T> clazz, Consumer<T> listener) {
        String id = UUID.randomUUID().toString();

        synchronized (listeners) {
            listeners.put(id, listener);
        }

        List<String> ids = listenersCache.getOrDefault(clazz, new ArrayList<>());

        ids.add(id);
        listenersCache.put(clazz, ids);

        return id;
    }

    public <T> T runEvent(T listener) {
        listenersCache.computeIfPresent(listener.getClass(), (list, ids) -> {
           ids.parallelStream().forEach(id -> {
               listeners.get(id).accept(listener);
           });

           return ids;
        });

        return listener;
    }
}
