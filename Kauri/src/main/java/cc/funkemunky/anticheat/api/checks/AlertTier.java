package cc.funkemunky.anticheat.api.checks;

import lombok.Getter;

@Getter
public enum AlertTier {
    LOW("Low", 0), POSSIBLE("Possible", 1), LIKELY("Likely", 2), HIGH("High", 3), CERTAIN("Certain", 4);

    private String name;
    private int priority;

    AlertTier(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}
