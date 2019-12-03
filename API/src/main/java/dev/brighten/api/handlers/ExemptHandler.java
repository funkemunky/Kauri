package dev.brighten.api.handlers;

import dev.brighten.api.KauriAPI;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.KauriCheck;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExemptHandler {

    private static List<Exemption> exemptList = new ArrayList<>();

    public Exemption addExemption(UUID uuid, KauriCheck... checks) {
        Exemption exemption = exemptList.stream()
                .filter(exempt -> exempt.uuid.equals(uuid))
                .findFirst().orElseGet(() -> new Exemption(uuid, checks));

        exemption.addChecks(checks);

        exemptList.add(exemption);

        return exemption;
    }

    //Adds temporary exception.
    public Exemption addExemption(UUID uuid, long millisLater, Consumer<Exemption> onRemove, KauriCheck... checks) {
        Exemption exemption = addExemption(uuid, checks);

        KauriAPI.INSTANCE.service.schedule(() -> {
            if(exemption.getChecks().size() == checks.length) {
                exemptList.remove(exemption);
            } else Arrays.stream(checks)
                    .filter(check -> exemption.getChecks().contains(check))
                    .forEach(check -> exemption.getChecks().remove(check));

            onRemove.accept(exemption);
        }, millisLater, TimeUnit.MILLISECONDS);

        return exemption;
    }

    public boolean isExempt(UUID uuid, KauriCheck... checks) {
        Exemption exemption = getExemption(uuid);

        return Arrays.stream(checks).anyMatch(check -> exemption.getChecks().contains(check));
    }

    public boolean isExempt(UUID uuid, CheckType... types) {
        return getExemption(uuid).getChecks().stream().
                anyMatch(check ->
                        Arrays.stream(types).anyMatch(type -> check.getCheckType().equals(type)));
    }

    public Exemption getExemption(UUID uuid) {
        return exemptList.stream().filter(exempt -> exempt.uuid.equals(uuid)).findFirst()
                .orElseGet(() -> addExemption(uuid));
    }
}
