package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.bungee.BungeeAPI;
import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_7;
import cc.funkemunky.api.tinyprotocol.api.packets.channelhandler.TinyProtocol1_8;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutCloseWindowPacket;
import cc.funkemunky.api.utils.*;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.discord.DiscordAPI;
import dev.brighten.anticheat.utils.Log;
import dev.brighten.anticheat.utils.api.BukkitAPI;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.TickTimer;
import dev.brighten.api.KauriAPI;
import dev.brighten.api.check.CancelType;
import dev.brighten.api.check.CheckType;
import dev.brighten.api.check.DevStage;
import dev.brighten.api.check.KauriCheck;
import dev.brighten.api.event.KauriEvent;
import dev.brighten.api.event.result.FlagResult;
import dev.brighten.api.event.result.PunishResult;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@NoArgsConstructor
public class Check implements KauriCheck {

    public static Map<WrappedClass, CheckInfo> checkClasses = new ConcurrentHashMap<>();
    public static Map<WrappedClass, CheckSettings> checkSettings = new ConcurrentHashMap<>();

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
    public DevStage devStage;
    @Getter
    @Setter
    public float vl, punishVl, vlToFlag;
    public ProtocolVersion minVersion, maxVersion;
    @Getter
    public CheckType checkType;

    public CancelType cancelMode;
    public List<String> executableCommands = new ArrayList<>();

    public boolean exempt, banExempt;

    protected final Timer lastAlert = new TickTimer(MathUtils.millisToTicks(Config.alertsDelay));

    private final Map<Class<? extends Check>, Check> detectionCache = new HashMap<>();


    public <T extends Check> T find(Class<? extends T> clazz) {
        Check check = detectionCache.computeIfAbsent(clazz, key -> {
            if(!clazz.isAnnotationPresent(CheckInfo.class)) {
                return null;
            }
            return data.checkManager.checks.get(clazz.getAnnotation(CheckInfo.class).name());
        });

        if(check != null)
        return clazz.cast(check);

        return null;
    }

    public void setData(ObjectData data) {
        this.data = data;
    }

    public static void register(Class<?> checkRawClass) {
        if(!checkRawClass.isAnnotationPresent(CheckInfo.class)) {
            Log.warning("Attempted to register class {} without CheckInfo annotations",
                    checkRawClass.getName());
            return;
        }
        CheckInfo info = checkRawClass.getAnnotation(CheckInfo.class);
        MiscUtils.printToConsole("Registering... " + info.name());
        WrappedClass checkClass = new WrappedClass(checkRawClass);
        String name = info.name();

        CancelType type = null;
        if(checkRawClass.isAnnotationPresent(Cancellable.class)) {
            type = checkRawClass.getAnnotation(Cancellable.class).cancelType();
        }

        String path = "checks." + name;
        CheckSettings settings = new CheckSettings(info.name(), info.description(), info.checkType(),
                info.devStage(), type, info.minVersion(), info.maxVersion());

        settings.enabled = new ConfigDefault<>(info.enabled(),
                path + ".enabled", Kauri.INSTANCE).get();
        settings.executable = new ConfigDefault<>(info.executable(),
                path + ".executable", Kauri.INSTANCE).get();
        settings.cancellable = new ConfigDefault<>(info.cancellable(),
                path + ".cancellable", Kauri.INSTANCE).get();
        settings.vlToFlag = new ConfigDefault<>(info.vlToFlag(),
                path + ".vlToFlag", Kauri.INSTANCE).get();
        settings.punishVl = new ConfigDefault<>(info.punishVL(),
                path + ".punishVl", Kauri.INSTANCE).get();
        settings.executableCommands = new ArrayList<>(new ConfigDefault<>(Arrays.asList("%global_commands%"),
                path + ".commands", Kauri.INSTANCE).get());

        runLoop(settings);

        final String spath = path + ".settings.";
        checkClass.getFields(field -> Modifier.isStatic(field.getModifiers())
                && field.isAnnotationPresent(Setting.class))
                .forEach(field -> {
                    Setting setting = field.getAnnotation(Setting.class);

                    MiscUtils.printToConsole("Found setting " + setting.name() + "! Processing...");
                    field.set(null, new ConfigDefault<>(field.get(null),
                            spath + setting.name(), Kauri.INSTANCE).get());
                });

        checkSettings.put(checkClass, settings);
        checkClasses.put(checkClass, info);
        MiscUtils.printToConsole("Registered check " + info.name());
    }

    private static void runLoop(CheckSettings settings) {
        for(String executableCommand : settings.executableCommands) {
            if(executableCommand.equals("%global_commands%")) {
                settings.executableCommands.remove(executableCommand);
                settings.executableCommands.addAll(Config.punishCommands);
                runLoop(settings);
                break;
            }
        }
    }

    public void flag() {
        flag(false, "");
    }

    public void flag(String information, Object... variables) {
        flag(false, information, variables);
    }

    public void flag(boolean devAlerts, String information, Object... variables) {
        flag(devAlerts, 100000000, information, variables);
    }

    public void flag(int resetVLTime, String information, Object... variables) {
        flag(false, resetVLTime, information, variables);
    }

    private String addPlaceHolders(String string) {
        return string.replace("%player%", data.getPlayer().getName())
                .replace("%check%", name)
                .replace("%name%", data.getPlayer().getName())
                .replace("%vl%", String.valueOf(MathUtils.round(vl, 1)))
                .replace("%experimental%", !devStage.isRelease()
                        ? "&c(" + devStage.getFormattedName() + ")" : "");
    }

    protected long lastFlagRun = 0L;

    public void flag(boolean devAlerts, int resetVLTime, String information, Object... variables) {
        Kauri.INSTANCE.loggingThread.execute(() -> {
            if(Kauri.INSTANCE.getTps() < 18)
                vl = 0;

            if(KauriAPI.INSTANCE.exemptHandler.isExempt(data.uuid, checkType)) return;
            if(System.currentTimeMillis() - lastFlagRun < 50L) return;
            lastFlagRun = System.currentTimeMillis();

            final String finalInformation = String.format(information, variables);

            FlagResult currentResult = FlagResult.builder().cancelled(false).build();

            final List<KauriEvent> events = KauriAPI.INSTANCE.getAllEvents();

            for (KauriEvent allEvent : events) {
                currentResult = allEvent
                        .onFlag(data.getPlayer(), this, information, currentResult.isCancelled());
            }

            if(currentResult.isCancelled()) return;

            if(cancellable && cancelMode != null && vl > vlToFlag) {
                cancelAction(cancelMode);
            }

            boolean dev = devAlerts || (!devStage.isRelease() || vl <= vlToFlag) || Kauri.INSTANCE.getTps() < 18;
            if(lastAlert.isPassed(resetVLTime)) vl = 0;
            final String info = finalInformation
                    .replace("%p", String.valueOf(data.lagInfo.transPing))
                    .replace("%t", String.valueOf(MathUtils.round(Kauri.INSTANCE.getTps(), 2)));
            if(vl > 0) Kauri.INSTANCE.loggerManager.addLog(data, this, info);

            if (lastAlert.isPassed(MathUtils.millisToTicks(Config.alertsDelay))) {
                //Sending Discord webhook alert
                if(DiscordAPI.INSTANCE != null)
                    DiscordAPI.INSTANCE.sendFlag(data.getPlayer(), this, dev, vl);
                List<TextComponent> components = new ArrayList<>();

                if(dev) {
                    components.add(new TextComponent(createTxt("&8[&cDev&8] ")));
                }
                val text = createTxt(Kauri.INSTANCE.msgHandler.getLanguage().msg("cheat-alert",
                        "&8[&6&lKauri&8] &f%player% &7flagged &f%check%" +
                                " &8(&ex%vl%&8) %experimental%"), info);

                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {
                        createTxt(Kauri.INSTANCE.msgHandler.getLanguage().msg("cheat-alert-hover",
                                "&eDescription&8: &f%desc%" +
                                        "\n&eInfo: &f%info%\n&r\n&7&oClick to teleport to player."), info)}));
                text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        formatAlert("/" + Config.alertCommand, info)));

                components.add(text);

                TextComponent[] toSend = components.toArray(new TextComponent[0]);

                if(Config.testMode && (dev ? !Kauri.INSTANCE.dataManager.hasAlerts.contains(data.uuid.hashCode())
                        : !Kauri.INSTANCE.dataManager.devAlerts.contains(data.uuid.hashCode())))
                    data.getPlayer().spigot().sendMessage(toSend);

                if(Config.alertsConsole) MiscUtils.printToConsole(new TextComponent(toSend).toPlainText());
                if(!dev) {
                    synchronized (Kauri.INSTANCE.dataManager.hasAlerts) {
                        for (int data : Kauri.INSTANCE.dataManager.hasAlerts.toArray(new int[0])) {
                            Kauri.INSTANCE.dataManager.dataMap.get(data).getPlayer().spigot().sendMessage(toSend);
                        }
                    }
                } else {
                    synchronized (Kauri.INSTANCE.dataManager.devAlerts) {
                        for (int data : Kauri.INSTANCE.dataManager.devAlerts.toArray(new int[0])) {
                            Kauri.INSTANCE.dataManager.dataMap.get(data).getPlayer().spigot().sendMessage(toSend);
                        }
                    }
                }
                lastAlert.reset();
            }

            punish();

            if (Config.bungeeAlerts) {
                try {
                    Atlas.getInstance().getBungeeManager()
                            .sendObjects("override", data.getPlayer().getUniqueId(), name,
                                    MathUtils.round(vl, 2), info);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private TextComponent createTxt(String txt) {
        return createTxt(txt, "");
    }
    private TextComponent createTxt(String txt, String info) {
        return new TextComponent(TextComponent.fromLegacyText(Color.translate(formatAlert(txt, info))));
    }

    public boolean isPosition(WrappedInFlyingPacket packet) {
        return packet.isPos() && (data.playerInfo.deltaXZ > 0 || data.playerInfo.deltaY != 0);
    }

    public void cancelAction(CancelType type) {
        cancelAction(type, false);
    }

    public void cancelAction(CancelType type, boolean overrideUserSetting) {
        if(!cancellable && !overrideUserSetting) return;

        switch(type) {
            case ATTACK: {
                for(int i = 0 ; i < 2 ; i++) {
                    synchronized (data.typesToCancel) {
                        data.typesToCancel.add(cancelMode);
                    }
                }
                break;
            }
            case INVENTORY: {
                TinyProtocolHandler.sendPacket(data.getPlayer(),
                        new WrappedOutCloseWindowPacket(data.playerInfo.inventoryId));
                break;
            }
            default: {
                synchronized (data.typesToCancel) {
                    data.typesToCancel.add(cancelMode);
                }
                break;
            }
        }
    }


    public void fixMovementBugs() {
        BukkitAPI.INSTANCE.setGliding(data.getPlayer(), false);
        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.v1_18))
        RunUtils.task(() -> {
            synchronized (data.blockInfo.blocks) {
                for (Block b : data.blockInfo.blocks) {
                    data.getPlayer()
                            .sendBlockChange(b.getLocation(), b.getType(), b.getData());
                }
            }
        });
    }

    private String formatAlert(String toFormat, String info) {
        return addPlaceHolders(Color.translate(toFormat.replace("%desc%", String.join("\n",
                        MiscUtils
                                .splitIntoLine(description, 20))))
                .replace("%info%", info));
    }

    public void punish() {
       Kauri.INSTANCE.loggingThread.execute(() -> {
           if(devStage.ordinal() > Arrays.stream(DevStage.values())
                   .filter(ds -> ds.name().equalsIgnoreCase(Config.minimumStageBan)).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("\"" + Config.minimumStageBan
                           + "\" is not a proper DevStage. Options: [Release, Beta, Alpha]")).ordinal()
                   || punishVl == -1 || vl <= punishVl
                   || System.currentTimeMillis() - Kauri.INSTANCE.lastTick > 200L) return;

           DiscordAPI.INSTANCE.sendBan(data.getPlayer(),  this, banExempt);

           vl = 0;

           if(!executable || (banExempt && Config.punishmentBypassPerm)) return;

           PunishResult punishResult = PunishResult.builder().cancelled(false)
                   .broadcastMessage(Config.broadcastMessage).commands(executableCommands).build();

           for(KauriEvent event : KauriAPI.INSTANCE.getAllEvents()) {
               PunishResult current = event.onPunish(data.getPlayer(), this, punishResult.getBroadcastMessage(),
                       punishResult.getCommands(), punishResult.isCancelled());

               punishResult = PunishResult.builder().cancelled(current.isCancelled())
                       .commands(current.getCommands() != null ? current.getCommands() : punishResult.getCommands())
                       .broadcastMessage(current.getBroadcastMessage() != null
                               ? current.getBroadcastMessage() : punishResult.getBroadcastMessage())
                       .build();
           }

           final String broadcastMessage = punishResult.getBroadcastMessage();
           final List<String> punishCommands = punishResult.getCommands();

           if(!punishResult.isCancelled()) {
               Kauri.INSTANCE.loggerManager.addPunishment(data, this);
               if(!data.banned) {
                   if(!Config.broadcastMessage.equalsIgnoreCase("off")) {
                       if (!Config.bungeeBroadcast) {
                           RunUtils.task(() -> {
                               if (!broadcastMessage.equalsIgnoreCase("off")) {
                                   Bukkit.broadcastMessage(Color.translate(addPlaceHolders(broadcastMessage)));
                               }
                           }, Kauri.INSTANCE);
                       } else {
                           BungeeAPI.broadcastMessage(Color.translate(addPlaceHolders(broadcastMessage)));
                       }
                   }
                   if(!Config.bungeePunishments) {
                       RunUtils.task(() -> {
                           ConsoleCommandSender sender = Bukkit.getConsoleSender();
                           punishCommands.
                                   forEach(cmd -> Bukkit.dispatchCommand(
                                           sender,
                                           addPlaceHolders(cmd.replace("%name%", data.getPlayer().getName()))));
                           vl = 0;
                       }, Kauri.INSTANCE);
                   } else {
                       punishCommands.
                               forEach(cmd -> BungeeAPI
                                       .sendCommand(addPlaceHolders(cmd.replace("%name%",
                                               data.getPlayer().getName()))));
                   }
                   data.banned = true;
                   RunUtils.taskLater(() -> {
                       if(data != null) data.banned = false;
                   }, Kauri.INSTANCE, 10);
               }
           }
       });
    }

    public void debug(String information, Object... variables) {
        if(data.debugging.size() == 0) return;

        Kauri.INSTANCE.loggingThread.execute(() -> {
            final String finalInformation = String.format(information, variables);

            synchronized (data.debugging) {
                for (Map.Entry<UUID, String> entry : data.debugging.entrySet()) {
                    if (entry.getValue().equals(name))
                        Kauri.INSTANCE.dataManager.dataMap.get(entry.getKey().hashCode()).getPlayer()
                                .sendMessage(Color.translate("&8[&c&lDEBUG&8] &7" + finalInformation));
                }
            }
        });
    }

    /** Player utils **/
    public void kickPlayer(String reason) {
        Bukkit.getLogger().log(Level.INFO, "Kauri is kicking player" + data.getPlayer().getName() + " for: \""
                + reason + "\"");
        RunUtils.task(() -> data.getPlayer().kickPlayer(Color.translate(reason)));
    }

    /** Static members **/
    public static void registerChecks() {

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

    public void closePlayerChannel() {
        val channel = getChannel.invoke(TinyProtocolHandler.getInstance(), data.getPlayer());

        val wrapped = new WrappedClass(channel.getClass());

        wrapped.getMethod("close").invoke(channel);
    }
}
