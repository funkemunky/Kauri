package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.impl.checks.combat.aimassist.AimA;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerA;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerB;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerC;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerD;
import cc.funkemunky.anticheat.impl.checks.combat.fastbow.Fastbow;
import cc.funkemunky.anticheat.impl.checks.combat.hitboxes.HitBox;
import cc.funkemunky.anticheat.impl.checks.combat.killaura.*;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachA;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachB;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachC;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachD;
import cc.funkemunky.anticheat.impl.checks.movement.*;
import cc.funkemunky.anticheat.impl.checks.player.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class CheckManager {
    private List<Check> checks = new ArrayList<>();
    private ExecutorService alertsExecutable;

    private Set<UUID> bypassingPlayers = new HashSet<>();

    public CheckManager() {
        alertsExecutable = Executors.newFixedThreadPool(2);
        checks = loadChecks();
    }

    public List<Check> loadChecks() {
        List<Check> checks = new ArrayList<>();
        checks.add(new AimA("Aim (Type A)", CheckType.AIM, CancelType.COMBAT, 80, true, false, false));
        checks.add(new AutoclickerA("Autoclicker (Type A)", CheckType.AUTOCLICKER, CancelType.COMBAT, 50, true, false ,true));
        checks.add(new AutoclickerB("Autoclicker (Type B)", CheckType.AUTOCLICKER, CancelType.COMBAT, 30, true, true, true));
        checks.add(new AutoclickerC("Autoclicker (Type C)", CheckType.AUTOCLICKER, CancelType.COMBAT, 40, true, false, true));
        checks.add(new AutoclickerD("Autoclicker (Type D)", CheckType.AUTOCLICKER, CancelType.COMBAT, 50, true, false, true));
        checks.add(new KillauraA("Killaura (Type A)", CheckType.KILLAURA, CancelType.COMBAT, 150, true, false, true));
        checks.add(new KillauraB("Killaura (Type B)", CheckType.KILLAURA, CancelType.COMBAT, 200, true, true, true));
        checks.add(new KillauraC("Killaura (Type C)", CheckType.KILLAURA, CancelType.COMBAT, 100, true, true, true));
        checks.add(new KillauraD("Killaura (Type D)", CheckType.KILLAURA, CancelType.COMBAT, 75, true, false, true));
        checks.add(new KillauraG("Killaura (Type G)", CheckType.KILLAURA, CancelType.COMBAT, 50, true, false, true));
        checks.add(new KillauraH("Killaura (Type H)", CheckType.KILLAURA, CancelType.COMBAT, 20, true, true, true));
        checks.add(new FlyA("Fly (Type A)", CheckType.FLY, CancelType.MOTION, 225, true, true, true));
        checks.add(new FlyB("Fly (Type B)", CheckType.FLY, CancelType.MOTION, 225, true, false, true));
        checks.add(new FlyC("Fly (Type C)", CheckType.FLY, CancelType.MOTION, 100, true, true, true));
        checks.add(new FlyD("Fly (Type D)", CheckType.FLY, CancelType.MOTION, 200, true, false, true));
        checks.add(new SpeedA("Speed (Type A)", CheckType.SPEED, CancelType.MOTION, 100, true, false, true));
        checks.add(new SpeedB("Speed (Type B)", CheckType.SPEED, CancelType.MOTION, 125, true, true, true));
        checks.add(new SpeedC("Speed (Type C)", CheckType.SPEED, CancelType.MOTION, 100, true, false, true));
        checks.add(new GroundSpoof("GroundSpoof", CheckType.MOVEMENT, CancelType.MOTION, 200, true, false, true));
        checks.add(new ReachA("Reach (Type A)", CheckType.REACH, CancelType.COMBAT, 60, true, true, true));
        checks.add(new ReachB("Reach (Type B)", CheckType.REACH, CancelType.COMBAT, 60, true, false, true));
        checks.add(new ReachC("Reach (Type C)", CheckType.REACH, CancelType.MOTION, 50, true, false, true));
        checks.add(new ReachD("Reach (Type D)", CheckType.REACH, CancelType.COMBAT, 50, true, true, true));
        checks.add(new Fastbow("Fastbow", CheckType.COMBAT, CancelType.INTERACT, 40, true, true, true));
        checks.add(new HitBox("HitBox", CheckType.COMBAT, CancelType.COMBAT, 30, true, false, true));
        checks.add(new BadPacketsA("BadPackets (Type A)", CheckType.BADPACKETS, CancelType.MOTION, 40, true, true, true));
        checks.add(new BadPacketsC("BadPackets (Type C)", CheckType.BADPACKETS, CancelType.COMBAT, 5, true, true, true));
        checks.add(new BadPacketsD("BadPackets (Type D)", CheckType.BADPACKETS, CancelType.COMBAT, 50, true, true, true));
        checks.add(new BadPacketsE("BadPackets (Type E)", CheckType.BADPACKETS, CancelType.HEALTH, 20, true, true, true));
        checks.add(new BadPacketsF("BadPackets (Type F)", CheckType.BADPACKETS, CancelType.MOTION, 20, true, true, true));
        checks.add(new BadPacketsG("BadPackets (Type G)", CheckType.BADPACKETS, CancelType.NONE, 150, true, false, true));
        checks.add(new VelocityA("Velocity (Type A)", CheckType.VELOCITY, CancelType.MOTION,  40, true, true, true));
        checks.add(new VelocityB("Velocity (Type B)", CheckType.VELOCITY, CancelType.MOTION,  40, true, false, true));

        for (Check check : checks) {
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
        }
        return checks;
    }

    public void registerCheck(Check check) {
        checks.add(check);
    }

    public boolean isCheck(String name) {
        return checks.stream().anyMatch(check -> check.getName().equalsIgnoreCase(name));
    }

    public void loadChecksIntoData(PlayerData data) {
        List<Check> checks = loadChecks();

        data.getChecks().clear();

        checks.forEach(check -> check.setData(data));

        data.setChecks(checks);
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
        if(bypassing) {
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
