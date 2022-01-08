package dev.brighten.api.handlers;

import dev.brighten.api.KauriAPI;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.KauriCheck;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static net.minecraft.server.v1_8_R3.WorldType.types;

public class ExemptHandler {

    private static final Map<UUID, Exemption> exemptions = new HashMap<>();

    @Deprecated
    public Exemption addExemption(UUID uuid, KauriCheck... checks) {
        return addExemption(uuid, Arrays.stream(checks)
                .map(KauriCheck::getCheckType)
                .toArray(CheckType[]::new));
    }

    /**
     * Adds your array of CheckType enums to a Set. This set will be called to on every Check#flag.
     * If the check's type matches, it will not flag.
     * @param uuid java.utils.UUID
     * @param checks dev.brighten.api.CheckType...
     * @return dev.brighten.api.handlers.Exemption
     */
    public Exemption addExemption(UUID uuid, CheckType... checks) {
        return exemptions.compute(uuid, (key, exemption) -> {
            if(exemption == null) {
                return new Exemption(uuid, checks);
            }

            exemption.addChecks(checks);

            return exemption;
        });
    }

    /**
     * Removes all CheckType enums from Exemption object of the UUID provided.
     * @param uuid java.utils.UUID
     * @param checks dev.brighten.api.CheckType...
     * @return dev.brighten.api.handlers.Exemption
     */
    public Exemption removeExemption(UUID uuid, CheckType... checks) {
        return exemptions.compute(uuid, (key, exemption) -> {
            if(exemption == null) {
                return new Exemption(uuid, new CheckType[0]);
            }

            exemption.removeChecks(checks);

            return exemption;
        });
    }

    //Will return false initally, and then true when completed.
    public AtomicBoolean addExemption(UUID uuid, long timeLater, TimeUnit unitLater, CheckType... checks) {
        addExemption(uuid, checks);

        AtomicBoolean removed = new AtomicBoolean(false);
        KauriAPI.INSTANCE.service.schedule(() -> {
            removeExemption(uuid, checks);
            removed.set(true);
        }, timeLater, unitLater);

        return removed;
    }

    public boolean isExempt(UUID uuid, CheckType type) {
        Optional<Exemption> exemption = getPlayerExemption(uuid);

        return exemption.map(value -> value.getChecks().contains(type)).orElse(false);
    }

    public boolean isExempt(UUID uuid, CheckType... type) {
        for (CheckType checkType : type) {
            if(isExempt(uuid, checkType))
                return true;
        }
        return false;
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
