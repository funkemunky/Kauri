package dev.brighten.api.handlers;

import dev.brighten.api.check.CheckType;
import lombok.Getter;

import java.security.SecureRandom;
import java.util.*;

public class Exemption {
    public final UUID uuid;
    @Getter
    private final Set<CheckType> checks;
    private static SecureRandom randomizer = new SecureRandom();
    private final Map<Long, ExemptParameter> exemptParameters = Collections.synchronizedMap(new HashMap<>());

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

    public void removeChecks(CheckType... checks) {
        Arrays.asList(checks).forEach(this.checks::remove);
    }

    public long addExemptParameter(ExemptParameter param) {
        long key = randomizer.nextLong();

        exemptParameters.put(key, param);

        return key;
    }
}
