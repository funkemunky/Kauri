package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.api.utils.Messages;
import lombok.Getter;

@Getter
public enum AlertTier {
    LOW(Messages.alertTierLow, 0), POSSIBLE(Messages.alertTierPossible, 1), LIKELY(Messages.alertTierLikely, 2), HIGH(Messages.alertTierHigh, 3), CERTAIN(Messages.alertTierCertain, 4);

    private String name;
    private int priority;

    AlertTier(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}
