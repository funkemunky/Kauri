package cc.funkemunky.anticheat.api.log;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Log {
    private UUID uuid;
    private Map<String, Integer> violations = new HashMap<>();
    private Map<String, List<String>> alertLog = new HashMap<>();
    private boolean banned;
    private String bannedCheck = "";

    public Log(UUID uuid) {
        this.uuid = uuid;
        banned = false;
    }
}
