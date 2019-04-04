package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
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
        try {
            Object obj = checkClass.getConstructors()[0].newInstance();
            Check check = (Check) obj;
            CheckInfo info = checkClass.getAnnotation(CheckInfo.class);

            check.setEnabled(info.enabled());
            check.setExecutable(info.executable());
            check.setCancellable(info.cancellable());
            check.setDescription(info.description());
            check.setMaxVL(info.maxVL());
            check.setMaximum(info.maxVersion());
            check.setType(info.type());
            check.setCancelType(info.cancelType());
            check.setName(info.name());
            check.setDeveloper(info.developer());
            check.setMinimum(info.minVersion());
            check.setBanWave(info.banWave());
            check.setBanWaveThreshold(info.banWaveThreshold());

            Arrays.stream(check.getClass().getDeclaredFields()).filter(field -> {
                field.setAccessible(true);

                return field.isAnnotationPresent(Setting.class);
            }).forEach(field -> {
                try {
                    field.setAccessible(true);

                    String path = "checks." + check.getName() + ".settings." + field.getName();
                    if (Kauri.getInstance().getConfig().get(path) != null) {
                        Object val = Kauri.getInstance().getConfig().get(path);

                        if (val instanceof Double && field.get(check) instanceof Float) {
                            field.set(check, (float) (double) val);
                        } else {
                            field.set(check, val);
                        }
                    } else {
                        Kauri.getInstance().getConfig().set("checks." + check.getName() + ".settings." + field.getName(), field.get(check));
                        Kauri.getInstance().saveConfig();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

            check.loadFromConfig();

            if ((check.getMinimum() == null || ProtocolVersion.getGameVersion().isOrAbove(check.getMinimum())) && (check.getMaximum() == null || ProtocolVersion.getGameVersion().isBelow(check.getMaximum()))) {
                checkList.add(check);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean isCheck(String name) {
        return checks.stream().anyMatch(check -> check.getName().equalsIgnoreCase(name));
    }

    public void loadChecksIntoData(PlayerData data) {
        List<Check> checkList = new ArrayList<>();

        checkClasses.forEach(clazz -> registerCheck(clazz, checkList));

        checkList.forEach(check -> check.setData(data));

        checkList.stream().filter(check -> check.getClass().isAnnotationPresent(Packets.class)).forEach(check -> {
            Packets packets = check.getClass().getAnnotation(Packets.class);

            Arrays.stream(packets.packets()).forEach(packet -> {
                List<Check> checks = data.getPacketChecks().getOrDefault(packet, new ArrayList<>());

                checks.add(check);

                data.getPacketChecks().put(packet, checks);
            });
        });

        checkList.stream().filter(check -> check.getClass().isAnnotationPresent(BukkitEvents.class)).forEach(check -> {
            BukkitEvents events = check.getClass().getAnnotation(BukkitEvents.class);

            Arrays.stream(events.events()).forEach(event -> {
                List<Check> checks = data.getBukkitChecks().getOrDefault(event, new ArrayList<>());

                checks.add(check);

                data.getBukkitChecks().put(event, checks);
            });
        });
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
        if (bypassing) {
            bypassingPlayers.add(uuid);
        } else {
            bypassingPlayers.remove(uuid);
        }
    }

    public void setBypassing(UUID uuid) {
        setBypassing(uuid, !isBypassing(uuid));
    }

    public void removeCheck(String name) {
        Optional<Check> opCheck = checks.stream().filter(check -> check.getName().equalsIgnoreCase(name)).findFirst();

        opCheck.ifPresent(check -> checks.remove(check));
    }
}
