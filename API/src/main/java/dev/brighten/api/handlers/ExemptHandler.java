package dev.brighten.api.handlers;

import dev.brighten.api.KauriAPI;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.KauriCheck;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ExemptHandler {

    private static Map<UUID, Exemption> exemptions = new HashMap<>();

    @Deprecated
    public Exemption addExemption(UUID uuid, KauriCheck... checks) {
        return addExemption(uuid, Arrays.stream(checks)
                .map(KauriCheck::getCheckType)
                .toArray(CheckType[]::new));
    }

    public Exemption addExemption(UUID uuid, CheckType... checks) {
        Exemption exemption = new Exemption(uuid, checks);

        return exemptions.put(uuid, exemption);
    }

    //Adds temporary exception.
    @Deprecated
    public Exemption addExemption(UUID uuid, long millisLater, Consumer<Exemption> onRemove, KauriCheck... checks) {
        Exemption exemption = addExemption(uuid, checks);

        KauriAPI.INSTANCE.service.schedule(() -> {
            exemptions.remove(uuid);

            onRemove.accept(exemption);
        }, millisLater, TimeUnit.MILLISECONDS);

        return exemption;
    }

    //Will return false initally, and then true when completed.
    public AtomicBoolean addExemption(UUID uuid, long timeLater, TimeUnit unitLater, CheckType... checks) {
        addExemption(uuid, checks);

        AtomicBoolean removed = new AtomicBoolean(false);
        KauriAPI.INSTANCE.service.schedule(() -> {
            exemptions.remove(uuid);
        }, timeLater, unitLater);

        return removed;
    }

    @Deprecated
    public boolean isExempt(UUID uuid, KauriCheck... checks) {
        if(!exemptions.containsKey(uuid)) return false;

        return isExempt(uuid, Arrays.stream(checks).map(KauriCheck::getCheckType).toArray(CheckType[]::new));
    }

    public boolean isExempt(UUID uuid, CheckType... types) {
        Optional<Exemption> exemption = getPlayerExemption(uuid);

        return exemption.map(exempt -> Arrays.stream(types).anyMatch(type -> exempt.getChecks().contains(type)))
                .orElse(false);
    }

    @Deprecated
    public Exemption getExemption(UUID uuid) {
        return exemptions.getOrDefault(uuid, null);
    }

    public Optional<Exemption> getPlayerExemption(UUID uuid) {
        if(!exemptions.containsKey(uuid)) return Optional.empty();

        return Optional.of(exemptions.get(uuid));
    }
}
