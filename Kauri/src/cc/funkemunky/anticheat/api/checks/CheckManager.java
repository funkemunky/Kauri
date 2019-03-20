package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.BukkitEvents;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.impl.checks.combat.aimassist.*;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.*;
import cc.funkemunky.anticheat.impl.checks.combat.fastbow.Fastbow;
import cc.funkemunky.anticheat.impl.checks.combat.hitboxes.HitBox;
import cc.funkemunky.anticheat.impl.checks.combat.killaura.*;
import cc.funkemunky.anticheat.impl.checks.combat.reach.*;
import cc.funkemunky.anticheat.impl.checks.movement.*;
import cc.funkemunky.anticheat.impl.checks.movement.fly.*;
import cc.funkemunky.anticheat.impl.checks.movement.jesus.JesusA;
import cc.funkemunky.anticheat.impl.checks.movement.speed.SpeedA;
import cc.funkemunky.anticheat.impl.checks.movement.speed.SpeedB;
import cc.funkemunky.anticheat.impl.checks.movement.speed.SpeedC;
import cc.funkemunky.anticheat.impl.checks.movement.speed.SpeedD;
import cc.funkemunky.anticheat.impl.checks.player.badpackets.*;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class CheckManager {
    private List<Check> checks = new ArrayList<>();
    private Set<UUID> bypassingPlayers = new HashSet<>();
    private ExecutorService alertsExecutable;

    public CheckManager() {
        alertsExecutable = Executors.newFixedThreadPool(2);
    }

    public void registerCheck(Check check) {
        if ((check.getMinimum() == null || ProtocolVersion.getGameVersion().isOrAbove(check.getMinimum())) && (check.getMaximum() == null || ProtocolVersion.getGameVersion().isBelow(check.getMaximum()))) {
            checks.add(check);
        }
    }

    public boolean isCheck(String name) {
        return checks.stream().anyMatch(check -> check.getName().equalsIgnoreCase(name));
    }

    public void loadChecksIntoData(PlayerData data) {
        List<Check> checkList = new ArrayList<>(checks);

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
