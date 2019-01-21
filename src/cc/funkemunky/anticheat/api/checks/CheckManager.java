package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerA;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerB;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.AutoclickerC;
import cc.funkemunky.anticheat.impl.checks.combat.fastbow.Fastbow;
import cc.funkemunky.anticheat.impl.checks.combat.hitboxes.HitBox;
import cc.funkemunky.anticheat.impl.checks.combat.killaura.*;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachA;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachB;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachC;
import cc.funkemunky.anticheat.impl.checks.combat.reach.ReachD;
import cc.funkemunky.anticheat.impl.checks.movement.*;
import cc.funkemunky.anticheat.impl.checks.player.*;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public class CheckManager {
    private List<Check> checks = Lists.newArrayList();

    public void init() {
        checks = loadChecks();
    }

    private List<Check> loadChecks() {
        List<Check> checks = Lists.newArrayList();
        checks.add(new AutoclickerA("Autoclicker (Type A)", CancelType.COMBAT, 20));
        checks.add(new AutoclickerB("Autoclicker (Type B)", CancelType.COMBAT, 20));
        checks.add(new AutoclickerC("Autoclicker (Type C)", CancelType.COMBAT, 20));
        checks.add(new KillauraA("Killaura (Type A)", CancelType.COMBAT, 150));
        checks.add(new KillauraB("Killaura (Type B)", CancelType.COMBAT, 200));
        checks.add(new KillauraC("Killaura (Type C)", CancelType.COMBAT, 100));
        checks.add(new KillauraD("Killaura (Type D)", CancelType.COMBAT, 100));
        checks.add(new KillauraD("Killaura (Type D)", CancelType.COMBAT, 75));
        checks.add(new KillauraE("Killaura (Type E)", CancelType.COMBAT, 100));
        checks.add(new KillAuraF("Killaura (Type F)", CancelType.COMBAT, 4));
        checks.add(new Fly("Fly", CancelType.MOTION, 225));
        checks.add(new SpeedA("Speed (Type A)", CancelType.MOTION, 100));
        checks.add(new SpeedB("Speed (Type B)", CancelType.MOTION, 125));
        checks.add(new SpeedC("Speed (Type C)", CancelType.MOTION, 100));
        checks.add(new ReachA("Reach (Type A)", CancelType.COMBAT, 60));
        checks.add(new ReachB("Reach (Type B)", CancelType.COMBAT, 60));
        checks.add(new ReachC("Reach (Type C)", CancelType.MOTION, 50));
        checks.add(new ReachD("Reach (Type D)", CancelType.COMBAT, 50));
        checks.add(new TimerA("Timer (Type A)", CancelType.MOTION, 100));
        checks.add(new TimerB("Timer (Type B)", CancelType.MOTION, 200));
        checks.add(new NoFall("NoFall", CancelType.MOTION, 100));
        checks.add(new Regen("Regen", CancelType.HEALTH, 20));
        checks.add(new Fastbow("Fastbow", CancelType.INTERACT, 40));
        checks.add(new HitBox("HitBox", CancelType.COMBAT, 30));
        checks.add(new BadPacketsA("BadPackets (Type A)", CancelType.MOTION, 40));
        checks.add(new BadPacketsB("BadPackets (Type B)", CancelType.COMBAT, 50));

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

    public void reloadChecks() {
        checks.clear();
        checks = loadChecks();

        Kauri.getInstance().getDataManager().getDataObjects().forEach(this::loadChecksIntoData);
    }

    public void loadChecksIntoData(PlayerData data) {
        List<Check> checks = loadChecks();

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
