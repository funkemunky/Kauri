package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.api.data.PlayerData;
import lombok.Getter;
import lombok.Setter;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class CheckManager {
    private List<Class<?>> checkClasses = new ArrayList<>();
    private Collection<Check> checks = new TreeSet<>(Comparator.comparing(Check::getName, Collator.getInstance()));
    private Set<UUID> bypassingPlayers = new HashSet<>();
    private ExecutorService alertsExecutable;

    public CheckManager() {
        alertsExecutable = Executors.newFixedThreadPool(2);
    }

    public void registerCheck(Class<?> checkClass, Collection<Check> checkList) {

    }

    public boolean isCheck(String name) {
        return checks.stream().anyMatch(check -> check.getName().equalsIgnoreCase(name));
    }

    public void loadChecksIntoData(PlayerData data) {

    }

    public Check getCheck(String name) {
        return checks.stream().filter(check -> check.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isBypassing(PlayerData data) {
        return isBypassing(data.getUuid());
    }

    public boolean isBypassing(UUID uuid) {
        return bypassingPlayers.contains(uuid);
    }

    public void setBypassing(UUID uuid, boolean bypassing) {

    }

    public void setBypassing(UUID uuid) {
        setBypassing(uuid, !isBypassing(uuid));
    }

    public void removeCheck(String name) {

    }
}
