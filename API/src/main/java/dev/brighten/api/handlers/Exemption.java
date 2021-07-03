package dev.brighten.api.handlers;

import dev.brighten.api.check.CheckType;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Exemption {
    public final UUID uuid;
    @Getter
    private final Set<CheckType> checks;

    public Exemption(UUID uuid, CheckType... checks) {
        this.uuid = uuid;
        this.checks = new HashSet<>(Arrays.asList(checks));
    }

    public Exemption(UUID uuid) {
        this.uuid = uuid;
        checks = new HashSet<>();
    }

    public void addChecks(CheckType... checks) {
        this.checks.addAll(Arrays.asList(checks));
    }
}
