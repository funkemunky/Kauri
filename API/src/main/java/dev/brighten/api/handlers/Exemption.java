package dev.brighten.api.handlers;

import dev.brighten.api.check.KauriCheck;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Exemption {
    public final UUID uuid;
    @Getter
    private final Set<KauriCheck> checks;

    public Exemption(UUID uuid, KauriCheck... checks) {
        this.uuid = uuid;
        this.checks = new HashSet<>(Arrays.asList(checks));
    }

    public Exemption(UUID uuid) {
        this.uuid = uuid;
        checks = new HashSet<>();
    }

    public void addChecks(KauriCheck... checks) {
        this.checks.addAll(Arrays.asList(checks));
    }
}
