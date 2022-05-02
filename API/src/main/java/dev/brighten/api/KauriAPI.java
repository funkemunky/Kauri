package dev.brighten.api;

import cc.funkemunky.api.reflections.Reflections;
import dev.brighten.api.event.KauriEvent;
import dev.brighten.api.handlers.ExemptHandler;
import dev.brighten.api.wrappers.WrappedDataManager;
import dev.brighten.api.wrappers.WrappedKauri;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class KauriAPI {

    public static KauriAPI INSTANCE;

    public ExemptHandler exemptHandler;
    public ScheduledExecutorService service;
    public WrappedDataManager dataManager;
    WrappedKauri kauriPlugin;

    private final Map<String, List<KauriEvent>> registeredEvents = new HashMap<>();

    public KauriAPI() {
        INSTANCE = this;
        exemptHandler = new ExemptHandler();
        service = Executors.newSingleThreadScheduledExecutor();
        kauriPlugin = new WrappedKauri(Reflections.getClass("dev.brighten.anticheat.Kauri")
                .getFieldByName("INSTANCE").get(null));
        dataManager = kauriPlugin.getDataManager();
    }

    public void reloadChecksForPlayer(Player player) {
        dataManager.getData(player).reloadChecks();
    }

    public void unregisterEvents(Plugin plugin) {
        registeredEvents.remove(plugin.getName());
    }

    public void registerEvent(Plugin plugin, KauriEvent event) {
        registeredEvents.compute(plugin.getName(), (key, list) -> {
            if(list == null) {
                list = new ArrayList<>();
            }

            list.add(event);
            return list;
        });
    }

    public String getVersion() {
        return KauriAPI.INSTANCE.kauriPlugin.getPlugin().getDescription().getVersion();
    }

    public List<KauriEvent> getAllEvents() {
        final List<KauriEvent> allEvents = new ArrayList<>();

        synchronized (registeredEvents) {
            for (List<KauriEvent> events : registeredEvents.values()) {
                allEvents.addAll(events);
            }
        }

        allEvents.sort(Comparator.comparing(e -> e.priority().getSlot()));

        return allEvents;
    }
}
