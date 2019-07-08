package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Getter
@Setter
public class CheckManager {
    private List<Class<?>> checkClasses = new ArrayList<>();
    private Collection<Check> checks = new TreeSet<>(Comparator.comparing(Check::getName, Collator.getInstance()));
    private Set<UUID> bypassingPlayers = new HashSet<>();
    private ExecutorService checkExecutor = Executors.newSingleThreadExecutor();
    private List<PlayerData> alerts = new ArrayList<>();
    private List<PlayerData> devAlerts = new ArrayList<>();

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

            if(checkClass.isAnnotationPresent(Packets.class)) {
                Packets packet = checkClass.getAnnotation(Packets.class);

                Arrays.stream(packet.packets()).forEach(pack -> check.getPackets().add(pack));
            }

            if(checkClass.isAnnotationPresent(BukkitEvents.class)) {
                BukkitEvents events = checkClass.getAnnotation(BukkitEvents.class);

                Arrays.stream(events.events()).forEach(event -> check.getEvents().add(event));
            }

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

            new BukkitRunnable() {
                public void run() {
                    val dataToSort = Kauri.getInstance().getDataManager().getDataObjects().keySet().stream().map(key -> Kauri.getInstance().getDataManager().getDataObjects().get(key)).filter(data -> data.getPlayer().hasPermission("kauri.alerts") && data.isAlertsEnabled()).collect(Collectors.toList());

                    alerts = new ArrayList<>(dataToSort);
                    devAlerts = dataToSort.stream().filter(PlayerData::isDeveloperAlerts).collect(Collectors.toList());
                }
            }.runTaskTimerAsynchronously(Kauri.getInstance(), 40L, 30L);
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

        checkList.forEach(check -> {
            check.setData(data);
            data.getChecks().add(check);
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
