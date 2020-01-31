package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.bungee.BungeeAPI;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_7;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_8;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.check.impl.combat.aim.*;
import dev.brighten.anticheat.check.impl.combat.autoclicker.*;
import dev.brighten.anticheat.check.impl.combat.hand.HandA;
import dev.brighten.anticheat.check.impl.combat.hand.HandB;
import dev.brighten.anticheat.check.impl.combat.hand.HandC;
import dev.brighten.anticheat.check.impl.combat.hitbox.Hitboxes;
import dev.brighten.anticheat.check.impl.combat.killaura.KillauraA;
import dev.brighten.anticheat.check.impl.combat.killaura.KillauraB;
import dev.brighten.anticheat.check.impl.combat.killaura.KillauraC;
import dev.brighten.anticheat.check.impl.combat.reach.Reach;
import dev.brighten.anticheat.check.impl.movement.fly.*;
import dev.brighten.anticheat.check.impl.movement.general.FastLadder;
import dev.brighten.anticheat.check.impl.movement.general.Phase;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallA;
import dev.brighten.anticheat.check.impl.movement.nofall.NoFallB;
import dev.brighten.anticheat.check.impl.movement.speed.*;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityA;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityB;
import dev.brighten.anticheat.check.impl.movement.velocity.VelocityC;
import dev.brighten.anticheat.check.impl.packets.Timer;
import dev.brighten.anticheat.check.impl.packets.badpackets.*;
import dev.brighten.anticheat.check.impl.world.place.BlockPlace;
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
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class Check implements KauriCheck {

    public static Map<WrappedClass, CheckInfo> checkClasses = Collections.synchronizedMap(new HashMap<>());
    public static Map<WrappedClass, CheckSettings> checkSettings = Collections.synchronizedMap(new HashMap<>());

    private static WrappedClass protocolClass = ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_8)
            ? new WrappedClass(TinyProtocol1_7.class) : new WrappedClass(TinyProtocol1_8.class);
    private static WrappedMethod getChannel = protocolClass.getMethod("getChannel", Player.class);


    public ObjectData data;
    @Getter
    public String name, description;
    @Getter
    @Setter
    public boolean enabled, executable, cancellable;
    @Getter
    public boolean developer;
    @Getter
    @Setter
    public float vl, punishVl;
    public ProtocolVersion minVersion, maxVersion;
    @Getter
    public CheckType checkType;

    public CancelType cancelMode;

    public boolean exempt, banExempt;
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

        CancelType type = null;
        if(check.getClass().isAnnotationPresent(Cancellable.class))
            type = check.getClass().getAnnotation(Cancellable.class).cancelType();
        CheckSettings settings = new CheckSettings(info.name(), info.description(), info.checkType(), type,
                info.punishVL(), info.vlToFlag(), info.minVersion(), info.maxVersion());

        settings.enabled = new ConfigDefault<>(info.enabled(),
                "checks." + name + ".enabled", Kauri.INSTANCE).get();
        settings.executable = new ConfigDefault<>(info.executable(),
                "checks." + name + ".executable", Kauri.INSTANCE).get();
        settings.cancellable = new ConfigDefault<>(info.cancellable(),
                "checks." + name + ".cancellable", Kauri.INSTANCE).get();

        checkSettings.put(checkClass, settings);
        checkClasses.put(checkClass, info);
    }

    public void flag(String information, Object... variables) {
        if(lastExemptCheck.hasPassed()) exempt = KauriAPI.INSTANCE.exemptHandler.isExempt(data.uuid, this);
        if(exempt) return;
        for (int i = 0; i < variables.length; i++) {
            Object var = variables[i];

            information = information.replace("%" + (i + 1), String.valueOf(var));
        }
        String finalInformation = information;
        KauriFlagEvent event = new KauriFlagEvent(data.getPlayer(), this, finalInformation);

        event.setCancelled(!Config.alertDev);

        if(cancellable && cancelMode != null) {
            if(!cancelMode.equals(CancelType.ATTACK)) {
                data.typesToCancel.add(cancelMode);
            } else for(int i = 0 ; i < 2 ; i++) data.typesToCancel.add(cancelMode);
        }

        Atlas.getInstance().getEventManager().callEvent(event);
        Kauri.INSTANCE.executor.execute(() -> {
            if(!event.isCancelled()) {
                final String info = finalInformation
                        .replace("%p", String.valueOf(data.lagInfo.transPing))
                        .replace("%t", String.valueOf(MathUtils.round(Kauri.INSTANCE.tps, 2)));
                if (Kauri.INSTANCE.lastTickLag.hasPassed() && (data.lagInfo.lastPacketDrop.hasPassed(5)
                        || data.lagInfo.lastPingDrop.hasPassed(20))
                        && System.currentTimeMillis() - Kauri.INSTANCE.lastTick < 100L) {
                    Kauri.INSTANCE.loggerManager.addLog(data, this, info);

                    if (lastAlert.hasPassed(MathUtils.millisToTicks(Config.alertsDelay))) {
                        JsonMessage jmsg = new JsonMessage();

                        jmsg.addText(Color.translate(Kauri.INSTANCE.msgHandler.getLanguage().msg("cheat-alert",
                                "&8[&6K&8] &f%player% &7flagged &f%check% &8(&e%info%&8)" +
                                        " &8(&c%vl%&8] %experimental%")
                                .replace("%player%", data.getPlayer().getName())
                                .replace("%check%", name)
                                .replace("%info%", info)
                                .replace("%vl%", String.valueOf(MathUtils.round(vl, 2)))
                                .replace("%experimental%", developer ? "&c&o(Experimental)" : "")))
                                .addHoverText(Color.translate(Kauri.INSTANCE.msgHandler.getLanguage().msg(
                                        "cheat-alert-hover",
                                        "&eDescription&8: &f%desc%\n&r\n&7&oClick to teleport to player.")
                                        .replace("%desc%", String.join("\n",
                                                dev.brighten.anticheat.utils.MiscUtils
                                                        .splitIntoLine(description, 20)))
                                        .replace("%player%", data.getPlayer().getName())
                                        .replace("%check%", name)
                                        .replace("%info%", info)
                                        .replace("%vl%", String.valueOf(MathUtils.round(vl, 2)))
                                        .replace("%experimental%", developer ? "&c&o(Experimental)" : "")))
                                .setClickEvent(JsonMessage.ClickableType.RunCommand, "/" + Config.alertCommand
                                        .replace("%player%", data.getPlayer().getName())
                                        .replace("%check%", name)
                                        .replace("%info%", info)
                                        .replace("%vl%", String.valueOf(MathUtils.round(vl, 2))));

                        if(Config.testMode) jmsg.sendToPlayer(data.getPlayer());

                            Kauri.INSTANCE.dataManager.hasAlerts.parallelStream()
                                .forEach(data -> jmsg.sendToPlayer(data.getPlayer()));
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
        if(banExempt || developer || !executable || punishVl == -1 || vl <= punishVl
                || System.currentTimeMillis() - Kauri.INSTANCE.lastTick > 200L) return;


        Kauri.INSTANCE.loggerManager.addPunishment(data, this);
        if(!data.banned && !Config.bungeePunishments) {
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
            data.banned = true;
        } else {
            if(!Config.broadcastMessage.equalsIgnoreCase("off")) {
                BungeeAPI.broadcastMessage(Color.translate(Config.broadcastMessage
                        .replace("%name%", data.getPlayer().getName())));
                Config.punishCommands.
                        forEach(cmd -> BungeeAPI.sendCommand(cmd.replace("%name%", data.getPlayer().getName())));
            }
        }
    }

    public void debug(String information, Object... variables) {
        if(Kauri.INSTANCE.dataManager.debugging.size() == 0) return;
        for (int i = 0; i < variables.length; i++) {
            Object var = variables[i];

            information = information.replace("%" + (i + 1), String.valueOf(var));
        }
        String finalInformation = information;
        Kauri.INSTANCE.dataManager.debugging.stream()
                .filter(data -> data.debugged.equals(this.data.uuid) && data.debugging.equalsIgnoreCase(name))
                .forEach(data -> data.getPlayer()
                        .sendMessage(Color.translate("&8[&c&lDEBUG&8] &7" + finalInformation)));
    }

    public static void registerChecks() {
        register(new AutoclickerA());
        register(new AutoclickerB());
        register(new AutoclickerC());
        register(new AutoclickerD());
        register(new AutoclickerE());
        register(new AutoclickerF());
        register(new AutoclickerG());
        register(new AutoclickerH());
        register(new FlyA());
        register(new FlyB());
        register(new FlyC());
        register(new FlyD());
        register(new FlyE());
        register(new FlyF());
        register(new FlyF());
        register(new FastLadder());
        register(new NoFallA());
        register(new NoFallB());
        register(new Reach());
        register(new Hitboxes());
        register(new AimA());
        register(new AimB());
        register(new AimC());
        register(new AimD());
        register(new AimE());
        register(new AimF());
        register(new AimG());
        register(new AimH());
        register(new AimI());
        register(new SpeedA());
        register(new SpeedB());
        register(new SpeedC());
        register(new SpeedD());
        register(new SpeedE());
        register(new KillauraA());
        register(new KillauraB());
        register(new KillauraC());
        register(new Phase());
        register(new Timer());
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
        register(new VelocityA());
        register(new VelocityB());
        register(new VelocityC());
        register(new HandA());
        register(new HandB());
        register(new HandC());
        //register(new HealthSpoof());
        register(new BlockPlace());
        //register(new BookOp());
        //register(new BookEnchant());
        //register(new PacketSpam());
        //register(new SignOp());
        //register(new SignCrash());
        //register(new LargeMove());
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

    public void kickPlayer() {
        /*val channel = getChannel.invoke(TinyProtocolHandler.getInstance(), data.getPlayer());

        val wrapped = new WrappedClass(channel.getClass());

        wrapped.getMethod("close").invoke(channel);*/
    }
}
