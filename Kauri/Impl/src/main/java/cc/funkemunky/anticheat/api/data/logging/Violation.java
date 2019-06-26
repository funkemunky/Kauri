package cc.funkemunky.anticheat.api.data.logging;

import lombok.Getter;
import lombok.val;

import java.util.LinkedHashMap;
import java.util.Map;

public class Violation {
    @Getter
    private Map<String, Integer> violations = new LinkedHashMap<>();

    public void addViolation(String check) {
        addViolation(check, 1);
    }

    public void addViolation(String check, int count) {
        val vl = violations.getOrDefault(check, 0);

        violations.put(check, vl + count);
    }

    public int getViolation(String check) {
        return violations.getOrDefault(check, 0);
    }
}
