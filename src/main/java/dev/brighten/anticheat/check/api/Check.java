package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerA;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerB;
import dev.brighten.anticheat.check.impl.combat.autoclicker.AutoclickerC;
import dev.brighten.anticheat.check.impl.combat.reach.ReachA;
import dev.brighten.anticheat.check.impl.movement.fly.FlyA;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFall;
import dev.brighten.anticheat.data.ObjectData;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Check {

    public static Map<WrappedClass, CheckInfo> checkClasses = Collections.synchronizedMap(new HashMap<>());
    public static Map<WrappedClass, CheckSettings> checkSettings = Collections.synchronizedMap(new HashMap<>());

    public Check() {

    }

    public ObjectData data;
    public String name, description;
    public boolean enabled, executable;
    public float vl;

    private static void register(Check check) {
        if(!check.getClass().isAnnotationPresent(CheckInfo.class)) {
            MiscUtils.printToConsole("Could not register "  + check.getClass().getSimpleName() + " because @CheckInfo was not present.");
            return;
        }
        CheckInfo info = check.getClass().getAnnotation(CheckInfo.class);
        MiscUtils.printToConsole("Registered: " + info.name());
        WrappedClass checkClass = new WrappedClass(check.getClass());
        String name = checkClass.getClass().getSimpleName();
        CheckSettings settings = new CheckSettings();
        if(Kauri.INSTANCE.getConfig().contains("checks." + name)) {
            settings.enabled = Kauri.INSTANCE.getConfig().getBoolean("checks." + name + ".enabled");
            settings.executable = Kauri.INSTANCE.getConfig().getBoolean("checks." + name + ".executable");
        } else {
            Kauri.INSTANCE.getConfig().set("checks." + name + ".enabled", info.enabled());
            Kauri.INSTANCE.getConfig().set("checks." + name + ".executable", info.executable());
            Kauri.INSTANCE.saveConfig();

            settings.enabled = info.enabled();
            settings.executable = info.executable();
        }
        checkSettings.put(checkClass, settings);
        checkClasses.put(checkClass, info);
    }

    public void flag(String information) {
        final String info = information
                .replace("%p", String.valueOf(data.transPing))
                .replace("%t", String.valueOf(MathUtils.round(Kauri.INSTANCE.tps, 2)));
        if(Kauri.INSTANCE.lastTickLag.hasPassed() && (data.lagInfo.lastPacketDrop.hasPassed(5) || data.lagInfo.lastPingDrop.hasPassed())) {
            float vl = this.vl;
            Kauri.INSTANCE.dataManager.hasAlerts.forEach(data -> {
                data.getPlayer().sendMessage(Color.translate("&8[&6K&8] &f" + this.data.getPlayer().getName() + " &7flagged &f" + name + " &8(&e" + info + "&8) &8[&c" + vl + "&8]"));
            });
        }
    }

    public void punish() {
        Bukkit.broadcastMessage("Nibba " + data.getPlayer().getName() + " got banned hehe xd.");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kick " + data.getPlayer().getName() + "[Kauri] you suck");
        vl = 0;
    }

    public void debug(String information) {
        if(Kauri.INSTANCE.dataManager.debugging.size() == 0) return;
        Kauri.INSTANCE.dataManager.debugging.stream()
                .filter(data -> data.debugged.equals(data.uuid) && data.debugging.equalsIgnoreCase(name))
                .forEach(data -> {
                    data.getPlayer().sendMessage(Color.translate("&8[&c&lDEBUG&8] &7" + information));
                });
    }

    public static void registerChecks() {
        register(new AutoclickerA());
        register(new AutoclickerB());
        register(new AutoclickerC());
        register(new FlyA());
        register(new NoFall());
        register(new ReachA());
    }

    public static boolean isCheck(String name) {
        return checkClasses.values().stream().anyMatch(val -> val.name().equalsIgnoreCase(name));
    }
}
