package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.api.utils.Messages;
import cc.funkemunky.api.utils.ReflectionsUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
public enum AlertTier {
    LOW(Messages.low, 0), POSSIBLE(Messages.possible, 1), LIKELY(Messages.likely, 2), HIGH(Messages.high, 3), CERTAIN(Messages.certain, 4);

    @Setter
    private String name;
    private int priority;

    AlertTier(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public static AlertTier getByValue(int value) {
        return Arrays.stream(values()).filter(tier -> Math.abs(tier.priority - value) == 0).findFirst().orElse(AlertTier.LOW);
    }

    public static AlertTier getByName(String name) {
        return Arrays.stream(values()).filter(tier -> tier.getName().equalsIgnoreCase(name)).findFirst().orElse(AlertTier.LOW);
    }

    public String getName() {
        String msg = (String) ReflectionsUtil.getFieldValue(ReflectionsUtil.getFieldByName(Messages.class, name().toLowerCase()), null);
        return msg;
    }
}
