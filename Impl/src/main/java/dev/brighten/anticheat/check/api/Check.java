package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.bungee.BungeeAPI;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.impl.combat.aim.*;
import dev.brighten.anticheat.check.impl.combat.autoclicker.*;
import dev.brighten.anticheat.check.impl.combat.hand.HandA;
import dev.brighten.anticheat.check.impl.combat.hand.HandB;
import dev.brighten.anticheat.check.impl.combat.hand.HandC;
import dev.brighten.anticheat.check.impl.combat.hitbox.Hitboxes;
import dev.brighten.anticheat.check.impl.combat.reach.Reach;
import dev.brighten.anticheat.check.impl.movement.fly.*;
import dev.brighten.anticheat.check.impl.movement.general.FastLadder;
import dev.brighten.anticheat.check.impl.movement.general.NoSlowdown;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallA;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallB;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedA;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedB;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedC;
import dev.brighten.anticheat.check.impl.movement.speed.SpeedD;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityA;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityB;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityC;
import dev.brighten.anticheat.check.impl.packets.Timer;
import dev.brighten.anticheat.check.impl.packets.badpackets.*;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.api.KauriAPI;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.KauriCheck;
import dev.brighten.api.listener.KauriFlagEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class Check implements KauriCheck {

    public static Map<WrappedClass, CheckInfo> checkClasses = Collections.synchronizedMap(new HashMap<>());
    public static Map<WrappedClass, CheckSettings> checkSettings = Collections.synchronizedMap(new HashMap<>());

    public ObjectData data;
    @Getter
    public String name, description;
    @Getter
    @Setter
    public boolean enabled, executable;
    @Getter
    public boolean developer;
    @Getter
    @Setter
    public float vl, punishVl;
    public ProtocolVersion minVersion, maxVersion;
    @Getter
    public CheckType checkType;

    private boolean exempt;
    private TickTimer lastExemptCheck = new TickTimer(20);

    private TickTimer lastAlert = new TickTimer(MathUtils.millisToTicks(Config.alertsDelay));

    private static void register(Check check) {
        if(!check.getClass().isAnnotationPresent(CheckInfo.class)) {
            MiscUtils.printToConsole("Could not register "  + check.getClass().getSimpleName()
                    + " because @CheckInfo was not present.");
            return;
        }
        CheckInfo info = check.getClass().getAnnotation(CheckInfo.class);
        MiscUtils.printToConsole("Registered: " + info.name());
        WrappedClass checkClass = new WrappedClass(check.getClass());
        String name = info.name();
        CheckSettings settings = new CheckSettings(info.name(), info.description(), info.checkType(),
                info.punishVL(), info.minVersion(), info.maxVersion());

        if(Kauri.INSTANCE.getConfig().get("checks." + name + ".enabled") != null) {
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
        if(lastExemptCheck.hasPassed()) exempt = KauriAPI.INSTANCE.exemptHandler.isExempt(data.uuid, this);
        if(exempt) return;
        Kauri.INSTANCE.executor.execute(() -> {
            KauriFlagEvent event = new KauriFlagEvent(data.getPlayer(), this);

            Atlas.getInstance().getEventManager().callEvent(event);

            if(!event.isCancelled()) {
                final String info = information
                        .replace("%p", String.valueOf(data.lagInfo.transPing))
                        .replace("%t", String.valueOf(MathUtils.round(Kauri.INSTANCE.tps, 2)));
                if (Kauri.INSTANCE.lastTickLag.hasPassed() && (data.lagInfo.lastPacketDrop.hasPassed(5)
                        || data.lagInfo.lastPingDrop.hasPassed(20))
                        && System.currentTimeMillis() - Kauri.INSTANCE.lastTick < 100L) {
                    Kauri.INSTANCE.loggerManager.addLog(data, this, info);

                    if (lastAlert.hasPassed(MathUtils.millisToTicks(Config.alertsDelay))) {
                        String message = Color.translate(Kauri.INSTANCE.msgHandler.getLanguage()
                                .msg("cheat-alert",
                                        "&8[&6K&8] &f%player% &7flagged &f%check% &8(&e%info%&8) &8(&c%vl%&8] %experimental%")
                                .replace("%player%", data.getPlayer().getName())
                                .replace("%check%", name)
                                .replace("%info%", info)
                                .replace("%vl%", String.valueOf(MathUtils.round(vl, 2)))
                                .replace("%experimental%", developer ? "&c&o(Experimental)" : ""));

                        Kauri.INSTANCE.dataManager.hasAlerts.forEach(data -> data.getPlayer().sendMessage(message));
                        lastAlert.reset();
                    }

                    punish();

                    if (Config.bungeeAlerts) {
                        try {
                            Atlas.getInstance().getBungeeManager()
                                    .sendObjects("ALL", data.getPlayer().getUniqueId(), name,
                                            MathUtils.round(vl, 2), info);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void punish() {
        if(developer || !executable || punishVl == -1 || vl <= punishVl
                || System.currentTimeMillis() - Kauri.INSTANCE.lastTick > 200L) return;

        Kauri.INSTANCE.loggerManager.addPunishment(data, this);
        if(!Config.bungeePunishments) {
            RunUtils.task(() -> {
                if(!Config.broadcastMessage.equalsIgnoreCase("off")) {
                    Bukkit.broadcastMessage(Color.translate(Config.broadcastMessage
                            .replace("%name%", data.getPlayer().getName())));
                }
                ConsoleCommandSender sender = Bukkit.getConsoleSender();
                Config.punishCommands.
                        forEach(cmd -> Bukkit.dispatchCommand(
                                sender,
                                cmd.replace("%name%", data.getPlayer().getName())));
                vl = 0;
            }, Kauri.INSTANCE);
        } else {
            if(!Config.broadcastMessage.equalsIgnoreCase("off")) {
                BungeeAPI.broadcastMessage(Color.translate(Config.broadcastMessage
                        .replace("%name%", data.getPlayer().getName())));
                Config.punishCommands.
                        forEach(cmd -> BungeeAPI.sendCommand(cmd.replace("%name%", data.getPlayer().getName())));
            }
        }
    }

    public void debug(String information) {
        if(Kauri.INSTANCE.dataManager.debugging.size() == 0) return;
        Kauri.INSTANCE.dataManager.debugging.stream()
                .filter(data -> data.debugged.equals(this.data.uuid) && data.debugging.equalsIgnoreCase(name))
                .forEach(data -> data.getPlayer().sendMessage(Color.translate("&8[&c&lDEBUG&8] &7" + information)));
    }

    public static void registerChecks() {
        register(new AutoclickerA());
        register(new AutoclickerB());
        register(new AutoclickerC());
        register(new AutoclickerD());
        register(new AutoclickerE());
        register(new FlyA());
        register(new FlyB());
        register(new FlyC());
        register(new FlyD());
        register(new FlyE());
        register(new FastLadder());
        register(new NoFallA());
        register(new NoFallB());
        register(new Reach());
        register(new Hitboxes());
        register(new AimA());
        register(new AimB());
        register(new AimC());
        register(new AimD());
        register(new AimF());
        register(new AimG());
        register(new AimH());
        register(new AimI());
        register(new AimJ());
        register(new SpeedA());
        register(new SpeedB());
        register(new SpeedC());
        register(new SpeedD());
        register(new Timer());
        register(new NoSlowdown());
        register(new BadPacketsA());
        register(new BadPacketsB());
        register(new BadPacketsC());
        register(new BadPacketsD());
        register(new BadPacketsE());
        register(new BadPacketsF());
        register(new BadPacketsG());
        register(new BadPacketsH());
        register(new BadPacketsI());
        register(new BadPacketsJ());
        register(new BadPacketsK());
        register(new BadPacketsL());
        register(new BadPacketsM());
        register(new BadPacketsN());
        register(new VelocityA());
        register(new VelocityB());
        register(new VelocityC());
        register(new HandA());
        register(new HandB());
        register(new HandC());
    }

    public static boolean isCheck(String name) {
        return checkClasses.values().stream().anyMatch(val -> val.name().equalsIgnoreCase(name));
    }

    public static CheckInfo getCheckInfo(String name) {
        return checkClasses.values().stream().filter(val -> val.name().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public static CheckSettings getCheckSettings(String name) {
        return checkSettings.values().stream().filter(val -> val.name.equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}
