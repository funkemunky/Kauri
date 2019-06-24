package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.api.utils.Messages;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AlertTier {
    LOW(Messages.alertTierLow, 0), POSSIBLE(Messages.alertTierPossible, 1), LIKELY(Messages.alertTierLikely, 2), HIGH(Messages.alertTierHigh, 3), CERTAIN(Messages.alertTierCertain, 4);

    private String name;
    private int priority;

    AlertTier(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public static AlertTier getByValue(int value) {
        return Arrays.stream(values()).filter(tier -> Math.abs(tier.priority - value) == 0).findFirst().orElse(AlertTier.LOW);
    }
}
