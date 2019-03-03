package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.anticheat.impl.checks.combat.aimassist.*;
import cc.funkemunky.anticheat.impl.checks.combat.autoclicker.*;
import cc.funkemunky.anticheat.impl.checks.combat.fastbow.Fastbow;
import cc.funkemunky.anticheat.impl.checks.combat.hitboxes.HitBox;
import cc.funkemunky.anticheat.impl.checks.combat.killaura.*;
import cc.funkemunky.anticheat.impl.checks.combat.reach.*;
import cc.funkemunky.anticheat.impl.checks.movement.*;
import cc.funkemunky.anticheat.impl.checks.player.*;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
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
        registerCheck(checks, new AimA("Aim (Type A)", "Checks for the consistency in aim overall", CheckType.AIM, CancelType.COMBAT, 80, true, false, true));
        registerCheck(checks, new AimB("Aim (Type B)", "Checks for common denominators in the pitch.", CheckType.AIM, CancelType.COMBAT, 50, true, true, true));
        registerCheck(checks, new AimC("Aim (Type C)", "Makes sure the aim acceleration is legitimate", CheckType.AIM, CancelType.COMBAT, 20, true, true, true));
        registerCheck(checks, new AimD("Aim (Type D)", "Checks for impossible pitch acceleration", CheckType.AIM, CancelType.COMBAT, 50, true, true, true));
        registerCheck(checks, new AimE("Aim (Type E)", "Looks for suspicious yaw and pitch movements. Not recommended for banning.", CheckType.AIM, CancelType.COMBAT, 200, true, false, false));
        registerCheck(checks, new AimF("Aim (Type F)", "Looks for any common variables.", CheckType.AIM, CancelType.COMBAT, 100, true, false, true));
        registerCheck(checks, new AutoclickerA("Autoclicker (Type A)", "A unique fast click check that detects jumps in CPS much faster.", CheckType.AUTOCLICKER, CancelType.COMBAT, 20, true, true, true));
        registerCheck(checks, new AutoclickerB("Autoclicker (Type B)", "Looks for suspicious consistencies in CPS averages.", CheckType.AUTOCLICKER, CancelType.COMBAT, 20, true, true, true));
        registerCheck(checks, new AutoclickerC("Autoclicker (Type C)", "An overall average CPS check.", CheckType.AUTOCLICKER, CancelType.COMBAT, 20, true, false, true));
        registerCheck(checks, new AutoclickerD("Autoclicker (Type D)", "Checks for very common autoclicker mistakes.", CheckType.AUTOCLICKER, CancelType.COMBAT, 20, true, false, true));
        registerCheck(checks, new AutoclickerE("Autoclicker (Type E)", "An unreasonable amount of CPS while breaking a block.", CheckType.AUTOCLICKER, CancelType.COMBAT, 20, true, true, true));
        registerCheck(checks, new AutoclickerF("Autoclicker (Type F)", "Compares the CPS of an autoclicker a certain frequency.", CheckType.AUTOCLICKER, CancelType.COMBAT, 5, true, true, true));
        registerCheck(checks, new AutoclickerG("Autoclicker (Type G)", "A normal click consistency check.", CheckType.AUTOCLICKER, CancelType.COMBAT, 50, true, false, true));
        registerCheck(checks, new KillauraA("Killaura (Type A)", "Checks the time between certain packets and attacks.", CheckType.KILLAURA, CancelType.COMBAT, 150, true, true, true));
        registerCheck(checks, new KillauraB("Killaura (Type B)", "Checks for an overall flaw in the rotations of many killauras", CheckType.KILLAURA, CancelType.COMBAT, 200, true, true, true));
        registerCheck(checks, new KillauraC("Killaura (Type C)", "Checks for clients sprinting while attacking.", CheckType.KILLAURA, CancelType.COMBAT, 100, true, true, true));
        registerCheck(checks, new KillauraD("Killaura (Type D)", "Detects over-randomization in killauras.", CheckType.KILLAURA, CancelType.COMBAT, 100, true, true, true));
        //registerCheck(checks, new KillauraE("Killaura (Type E)", CheckType.KILLAURA, CancelType.COMBAT, 100, true, true, true));
        //registerCheck(checks, new KillauraF("Killaura (Type F)", CheckType.KILLAURA, CancelType.COMBAT, 12));
        registerCheck(checks, new KillauraG("Killaura (Type G)", "Raytraces to check if there are blocks obstructing the path of attack.", CheckType.KILLAURA, CancelType.COMBAT, 50, true, false, true));
        registerCheck(checks, new KillauraH("Killaura (Type H)", "Detects if clients are swinging impossibly.", CheckType.KILLAURA, CancelType.COMBAT, 3, true, true, true));
        registerCheck(checks, new FlyA("Fly (Type A)", "An acceleration check for flight.", CheckType.FLY, CancelType.MOTION, 225, true, true, true));
        registerCheck(checks, new FlyB("Fly (Type B)", "Calculates what the actual vertical speed of a player should be.", CheckType.FLY, CancelType.MOTION, 225, true, false, true));
        registerCheck(checks, new FlyC("Fly (Type C)", "A different style of acceleration check.", CheckType.FLY, CancelType.MOTION, 100, true, true, true));
        registerCheck(checks, new FlyD("Fly (Type D)", "Makes sure the client is accelerating towards the ground properly.", CheckType.FLY, CancelType.MOTION, 200, true, true, true));
        registerCheck(checks, new FlyE("Fly (Type E)", "Checks if a client moves vertically faster than what is possible.", CheckType.FLY, CancelType.MOTION, 150, true, true, true));
        registerCheck(checks, new FlyF("Fly (Type F)", "Prevents clients from using velocity exploits for an unfair advantage.", CheckType.FLY, CancelType.MOTION, 100, true, false, true));
        registerCheck(checks, new SpeedA("Speed (Type A)", "A basic maximum speed check with a verbose threshold.", CheckType.SPEED, CancelType.MOTION, 100, true, true, true));
        registerCheck(checks, new SpeedB("Speed (Type B)", "A simple but effective speed check.", CheckType.SPEED, CancelType.MOTION, 125, true, true, true));
        registerCheck(checks, new SpeedC("Speed (Type C)", "Checks the in-air horizontal deceleration of the client. More accurate.", CheckType.SPEED, CancelType.MOTION, 100, true, true, true));
        registerCheck(checks, new SpeedD("Speed (Type D)", "Checks the in-air and on-ground deceleration of the client. Less accurate.", CheckType.SPEED, CancelType.MOTION, 120, true, false, true));
        registerCheck(checks, new ReachA("Reach (Type A)", "A basic maximum reach calculation.", CheckType.REACH, CancelType.COMBAT, 60, true, true, true));
        registerCheck(checks, new ReachB("Reach (Type B)", "A simple and light, but extremely effective maximum reach calculation. However, slightly experimental.", CheckType.REACH, CancelType.COMBAT, 60, true, false, true));
        registerCheck(checks, new ReachC("Reach (Type C)", "Uses expanded bounding-boxes to set a maximum hit reach.", CheckType.REACH, CancelType.MOTION, 50, false, true, true));
        registerCheck(checks, new ReachD("Reach (Type D)", "Uses a mixture of lighter but less accurate ray-tracing to determine the client's actual reach distance.", CheckType.REACH, CancelType.COMBAT, 50, false, true, true));
        registerCheck(checks, new ReachE("Reach (Type E)", "A ray-tracing check but is less light, however it detects 3.1 reach very accurately.", CheckType.REACH, CancelType.COMBAT, 50, true, true, true));
        //registerCheck(checks, new TimerB("Timer (Type B)", CancelType.MOTION, 200));
        registerCheck(checks, new Fastbow("Fastbow", "Makes sure the rate of fire is legitimate.", CheckType.COMBAT, CancelType.INTERACT, 40, true, true, true));
        registerCheck(checks, new HitBox("HitBox", "A very accurate hit-box check, using a mixture of ray-tracing and bounding-boxes.", CheckType.COMBAT, CancelType.COMBAT, 30, true, true, true));
        registerCheck(checks, new BadPacketsA("BadPackets (Type A)", "Prevents the client from spoofing the ability to fly.", CheckType.BADPACKETS, CancelType.MOTION, 40, true, true, true));
        registerCheck(checks, new BadPacketsC("BadPackets (Type C)", "Checks for impossible pitch rotation.", CheckType.BADPACKETS, CancelType.COMBAT, 50, true, true, true));
        registerCheck(checks, new BadPacketsD("BadPackets (Type D)", "Compares the rate of interact packets to a certain frequency.", CheckType.BADPACKETS, CancelType.INTERACT, 50, true, true, true));
        registerCheck(checks, new BadPacketsE("BadPackets (Type E)", "Checks the rate of healing.", CheckType.BADPACKETS, CancelType.HEALTH, 20, true, true, true));
        registerCheck(checks, new BadPacketsF("BadPackets (Type F)", "Checks frequency of incoming packets. More reliable, but less detection.", CheckType.BADPACKETS, CancelType.MOTION, 100, true, true, true));
        registerCheck(checks, new Timer("BadPackets (Type G)", "Checks frequency of incoming packets. More detection, but less reliable.", CheckType.BADPACKETS, CancelType.MOTION, 20, true, false, true));
        registerCheck(checks, new VelocityA("Velocity (Type A)", "Detects any vertical velocity modification below 100%.", CheckType.VELOCITY, CancelType.MOTION,  40, true, true, true));
        registerCheck(checks, new VelocityB("Velocity (Type B)", "Checks for horizontal velocity modifications.", CheckType.VELOCITY, CancelType.MOTION,  40, true, false, true));

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

    public void registerCheck(List<Check> checks, Check check) {
        if((check.getMinimum() == null || ProtocolVersion.getGameVersion().isOrAbove(check.getMinimum())) && (check.getMaximum() == null || ProtocolVersion.getGameVersion().isBelow(check.getMaximum()))) {
            checks.add(check);
        }
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
