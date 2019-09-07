package dev.brighten.anticheat.api.check;

import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MathUtils;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.impl.check.combat.autoclicker.AutoclickerA;
import org.bukkit.Bukkit;

import java.util.*;

public class Check {

    public static Map<WrappedClass, CheckInfo> checkClasses = Collections.synchronizedMap(new HashMap<>());

    public Check() {

    }

    public ObjectData data;
    public String name, description;
    public boolean enabled, executable;
    public float vl, banVL;


    private static void register(Check check) {
        if(!check.getClass().isAnnotationPresent(CheckInfo.class)) {
            MiscUtils.printToConsole("Could not register "  + check.getClass().getSimpleName() + " because @CheckInfo was not present.");
            return;
        }
        CheckInfo info = check.getClass().getAnnotation(CheckInfo.class);
        MiscUtils.printToConsole("Registered: " + info.name());
        checkClasses.put(new WrappedClass(check.getClass()), info);
    }

    public void flag(String information) {
        final String info = information.replace("%p", String.valueOf(data.transPing)).replace("%t", String.valueOf(MathUtils.round(Kauri.INSTANCE.tps, 2)));
        Kauri.INSTANCE.dataManager.hasAlerts.stream().forEach(data -> {
            data.getPlayer().sendMessage(Color.translate("&8[&6K&8] &f" + data.getPlayer().getName() + " &7flagged &f" + name + " &8(&e" + info + "&8) &8[&c" + vl + "&8]"));
        });
    }

    public void punish() {
        Bukkit.broadcastMessage("Nibba " + data.getPlayer().getName() + " got banned hehe xd.");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + data.getPlayer().getName() + "[Kauri] you suck");
    }

    public static void registerChecks() {
        register(new AutoclickerA());
    }
}
