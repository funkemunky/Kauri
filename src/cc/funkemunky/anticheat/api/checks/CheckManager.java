package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.impl.checks.combat.aimassist.AimA;
import cc.funkemunky.anticheat.impl.checks.combat.aimassist.AimB;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class CheckManager {
    private List<Check> checks = new ArrayList<>();

    public CheckManager() {
        checks = loadChecks();
    }

    public List<Check> loadChecks() {
        List<Check> checks = new ArrayList<>();
        checks.add(new AimA("Aim (Type A)", CheckType.AIM, CancelType.COMBAT, 80));
        checks.add(new AimB("Aim (Type B)", CheckType.AIM, CancelType.COMBAT, 50));
        checks.add(new AutoclickerA("Autoclicker (Type A)", CheckType.AUTOCLICKER, CancelType.COMBAT, 20));
        checks.add(new AutoclickerB("Autoclicker (Type B)", CheckType.AUTOCLICKER, CancelType.COMBAT, 20));
        checks.add(new AutoclickerC("Autoclicker (Type C)", CheckType.AUTOCLICKER, CancelType.COMBAT, 20));
        checks.add(new AutoclickerD("Autoclicker (Type D)", CheckType.AUTOCLICKER, CancelType.COMBAT, 20));
        checks.add(new AutoclickerE("Autoclicker (Type E)", CheckType.AUTOCLICKER, CancelType.COMBAT, 20));
        checks.add(new KillauraA("Killaura (Type A)", CheckType.KILLAURA, CancelType.COMBAT, 150));
        checks.add(new KillauraB("Killaura (Type B)", CheckType.KILLAURA, CancelType.COMBAT, 200));
        checks.add(new KillauraC("Killaura (Type C)", CheckType.KILLAURA, CancelType.COMBAT, 100));
        checks.add(new KillauraD("Killaura (Type D)", CheckType.KILLAURA, CancelType.COMBAT, 100));
        checks.add(new KillauraD("Killaura (Type D)", CheckType.KILLAURA, CancelType.COMBAT, 75));
        checks.add(new KillauraE("Killaura (Type E)", CheckType.KILLAURA, CancelType.COMBAT, 100, true, true, true));
        checks.add(new KillauraF("Killaura (Type F)", CheckType.KILLAURA, CancelType.COMBAT, 12));
        checks.add(new KillauraG("Killaura (Type G)", CheckType.KILLAURA, CancelType.COMBAT, 50, true, false, true));
        checks.add(new KillauraH("Killaura (Type H)", CheckType.KILLAURA, CancelType.COMBAT, 3));
        checks.add(new FlyA("Fly (Type A)", CheckType.MOVEMENT, CancelType.MOTION, 225));
        checks.add(new FlyB("Fly (Type B)", CheckType.MOVEMENT, CancelType.MOTION, 225, true, false, true));
        checks.add(new SpeedA("Speed (Type A)", CheckType.MOVEMENT, CancelType.MOTION, 100));
        checks.add(new SpeedB("Speed (Type B)", CheckType.MOVEMENT, CancelType.MOTION, 125));
        checks.add(new SpeedC("Speed (Type C)", CheckType.MOVEMENT, CancelType.MOTION, 100));
        checks.add(new ReachA("Reach (Type A)", CheckType.REACH, CancelType.COMBAT, 60));
        checks.add(new ReachB("Reach (Type B)", CheckType.REACH, CancelType.COMBAT, 60));
        checks.add(new ReachC("Reach (Type C)", CheckType.REACH, CancelType.MOTION, 50));
        checks.add(new ReachD("Reach (Type D)", CheckType.REACH, CancelType.COMBAT, 50));
        //checks.add(new TimerB("Timer (Type B)", CancelType.MOTION, 200));
        checks.add(new NoFall("NoFall", CheckType.MOVEMENT, CancelType.MOTION, 100));
        checks.add(new Fastbow("Fastbow", CheckType.COMBAT, CancelType.INTERACT, 40));
        checks.add(new HitBox("HitBox", CheckType.COMBAT, CancelType.COMBAT, 30));
        checks.add(new BadPacketsA("BadPackets (Type A)", CheckType.BADPACKETS, CancelType.MOTION, 40));
        checks.add(new BadPacketsB("BadPackets (Type B)", CheckType.BADPACKETS, CancelType.COMBAT, 50));
        checks.add(new BadPacketsC("BadPackets (Type C)", CheckType.BADPACKETS, CancelType.COMBAT, 50));
        checks.add(new BadPacketsD("BadPackets (Type D)", CheckType.BADPACKETS, CancelType.COMBAT, 50));
        checks.add(new BadPacketsE("BadPackets (Type E)", CheckType.BADPACKETS, CancelType.HEALTH, 20));
        checks.add(new BadPacketsF("BadPackets (Type F)", CheckType.BADPACKETS, CancelType.MOTION, 100));
        checks.add(new Timer("Timer", CheckType.BADPACKETS, CancelType.NONE, 20));
        checks.add(new VelocityH("Velocity (Type H)", CheckType.VELOCITY, CancelType.MOTION,  40));
        checks.add(new VelocityV("Velocity (Type v)", CheckType.VELOCITY, CancelType.MOTION,  40));

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

    public void removeCheck(String name) {
        Optional<Check> opCheck = checks.stream().filter(check -> check.getName().equalsIgnoreCase(name)).findFirst();

        opCheck.ifPresent(check -> checks.remove(check));
    }
}
