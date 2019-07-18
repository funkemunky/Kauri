package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckManager;
import cc.funkemunky.anticheat.api.data.DataManager;
import cc.funkemunky.anticheat.api.data.banwave.BanwaveManager;
import cc.funkemunky.anticheat.api.data.logging.LoggerManager;
import cc.funkemunky.anticheat.api.data.stats.StatsManager;
import cc.funkemunky.anticheat.api.pup.AntiPUPManager;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.api.utils.VPNUtils;
import cc.funkemunky.anticheat.impl.commands.kauri.KauriCommand;
import cc.funkemunky.anticheat.impl.listeners.LegacyListeners;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventManager;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.profiling.BaseProfiler;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutEntityMetadata;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedWatchableObject;
import cc.funkemunky.api.tinyprotocol.reflection.Reflection;
import cc.funkemunky.api.updater.UpdaterUtils;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import me.mat1337.loader.Loader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class Kauri extends JavaPlugin {
    @Getter
    private static Kauri instance;

    private DataManager dataManager;
    private CheckManager checkManager;
    private StatsManager statsManager;
    private AntiPUPManager antiPUPManager;
    private LoggerManager loggerManager;
    private BanwaveManager banwaveManager;

    private int ticks;
    private long profileStart, lastTimeStamp, tpsMilliseconds;
    private double tps;

    private ScheduledExecutorService executorService, vpnSchedular = Executors.newSingleThreadScheduledExecutor();

    private BaseProfiler profiler;
    private VPNUtils vpnUtils;

    private String requiredVersionOfAtlas = "1.3.6";
    private List<String> usableVersionsOfAtlas = Arrays.asList("1.3.4", "1.3.5", "1.3.6");

    private FileConfiguration messages;
    private File messagesFile;
    public ExecutorService dedicatedVPN = Executors.newSingleThreadExecutor();
    public long lastLogin;

    private boolean testMode = true, runningPaperSpigot;

    @Override
    public void onEnable() {
        //This allows us to access this class's contents from others places.
        instance = this;
        saveDefaultConfig();
        saveDefaultMessages();

        //if (Bukkit.getPluginManager().getPlugin("KauriLoader") == null || !Bukkit.getPluginManager().getPlugin("KauriLoader").isEnabled()) return;

        if(Bukkit.getVersion().contains("Paper")) {
            runningPaperSpigot = true;
        }
        profiler = new BaseProfiler();
        profileStart = System.currentTimeMillis();

        executorService = Executors.newScheduledThreadPool(2);

        dataManager = new DataManager();
        checkManager = new CheckManager();

        startScanner(false);

        antiPUPManager = new AntiPUPManager();
        dataManager.registerAllPlayers();

        //Starting up our utilities, managers, and tasks.

        statsManager = new StatsManager();
        loggerManager = new LoggerManager(Atlas.getInstance().getCarbon());
        loggerManager.loadFromDatabase();
        banwaveManager = new BanwaveManager();

        vpnUtils = new VPNUtils();
        new KauriAPI();

        runTasks();
        registerCommands();
        registerListeners();
    }

    public void onDisable() {
        statsManager.saveStats();
        loggerManager.saveToDatabase();
        org.bukkit.event.HandlerList.unregisterAll(this);
        dataManager.getDataObjects().clear();
        checkManager.getChecks().clear();
        Atlas.getInstance().getCommandManager().unregisterCommands(this);
        executorService.shutdownNow();
    }

    private void runTasks() {
        //This allows us to use ticks for intervalTime comparisons to allow for more parrallel calculations to actual Minecraft
        //and it also has the added benefit of being lighter than using System.currentTimeMillis.
        new BukkitRunnable() {

            public void run() {
                if(ticks++ >= 39) {
                    long timeStamp = System.currentTimeMillis();
                    tpsMilliseconds = timeStamp - lastTimeStamp;
                    tps = 1000D / tpsMilliseconds * 40;
                    lastTimeStamp = timeStamp;
                    ticks = 0;
                }
            }
        }.runTaskTimer(this, 1L, 1L);
       if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) {
           new BukkitRunnable() {
               public void run() {
                   for (World world : Bukkit.getWorlds()) {
                       List<Player> players;
                       List<LivingEntity> entities;

                       players = (entities = new ArrayList<>(world.getLivingEntities())).stream()
                               .filter(ent -> ent instanceof Player && Bukkit.getOnlinePlayers().contains(ent))
                               .map(ent -> (Player) ent)
                               .collect(Collectors.toList());

                       Map<Entity, Object> packetsToSend = new HashMap<>();
                       Class<?> entityClass = ReflectionsUtil.getNMSClass("Entity");
                       entities.forEach(ent -> {
                           Object vanillaEnt = ReflectionsUtil.getEntity(ent);
                           Object dataWatcher = ReflectionsUtil.getFieldValue(ReflectionsUtil.getFieldByName(entityClass, "datawatcher"), vanillaEnt);

                           Map map = Reflection.getField(ReflectionsUtil.getNMSClass("DataWatcher"), Map.class, 1).get(dataWatcher);

                           List<Object> watchables = new ArrayList<>();

                           map.keySet().forEach(key -> watchables.add(map.get(key)));

                           if(watchables.size() > 7) {
                               WrappedOutEntityMetadata toSend = null;
                               WrappedWatchableObject object7 = new WrappedWatchableObject(watchables.get(7)), object6 = new WrappedWatchableObject(watchables.get(6));

                               if(object7.getWatchedObject() instanceof Float) {
                                   object7.setWatchedObject(1.0f);
                                   object7.setPacket(NMSObject.Type.WATCHABLE_OBJECT, object7.getObjectType(), object7.getDataValueId(), object7.getWatchedObject());

                                   watchables.set(7, object7.getObject());
                                   toSend = new WrappedOutEntityMetadata(ent.getEntityId(), watchables);
                               } else if(object6.getWatchedObject() instanceof Float) {
                                   object6.setWatchedObject(1.0f);
                                   object6.setPacket(NMSObject.Type.WATCHABLE_OBJECT, object6.getObjectType(), object6.getDataValueId(), object6.getWatchedObject());

                                   watchables.set(6, object6.getObject());
                                   toSend = new WrappedOutEntityMetadata(ent.getEntityId(), watchables);
                               }

                               if(toSend != null) {
                                   packetsToSend.put(ent, toSend.getObject());
                               }
                           }
                       });
                       players.stream().forEach(pl -> packetsToSend.keySet().forEach(key -> {
                           if(!key.getUniqueId().equals(pl.getUniqueId())) {
                               TinyProtocolHandler.sendPacket(pl, packetsToSend.get(key));
                           }
                       }));
                   }
               }
           }.runTaskTimerAsynchronously(this, 20L, 30L);
       }
    }

    public void startScanner(boolean configOnly) {
        initializeScanner(getClass(), this, configOnly);
    }

    private void registerCommands() {
        Atlas.getInstance().getFunkeCommandManager().addCommand(this, new KauriCommand());
    }

    public double getTPS(RoundingMode mode, int places) {
        return MathUtils.round(tps, places, mode);
    }

    public double getTpsMS() {
        return 50 / (2000D / tpsMilliseconds);
    }

    public void reloadKauri(boolean reloadedMessages) {
        MiscUtils.printToConsole("&cReloading Kauri...");
        long start = System.currentTimeMillis();
        if(!reloadedMessages) {
            MiscUtils.printToConsole("&7Reloading configuration files...");
            reloadConfig();

            MiscUtils.printToConsole("&7Unregistering listeners...");
            HandlerList.unregisterAll(this);
            Atlas.getInstance().getEventManager().unregisterAll(this);

            MiscUtils.printToConsole("&7Unregistering tasks...");
            Bukkit.getScheduler().cancelTasks(this);
            //Clearing check manager garbage.
            MiscUtils.printToConsole("&7Clearing check garbage...");
            checkManager.getChecks().clear();
            checkManager.getCheckClasses().clear();
            checkManager.getDebuggingPackets().clear();
            checkManager.getDevAlerts().clear();
            checkManager.getAlerts().clear();
            checkManager.getBypassingPlayers().clear();
            checkManager = new CheckManager();

            //Clearing antiPuP garbage.
            MiscUtils.printToConsole("&7Clearing AntiPUP garbage...");
            antiPUPManager.pupThread.shutdown();
            antiPUPManager = new AntiPUPManager();
            MiscUtils.printToConsole("&7Clear PlayerData garbage...");
            dataManager.getDataObjects().clear();
            dataManager = new DataManager();
            MiscUtils.printToConsole("&7Resetting profiler...");
            profiler.reset();
            MiscUtils.printToConsole("&7Scanning for changes in config, messages, and to register checks...");
            startScanner(true);
            MiscUtils.printToConsole("&7Registering PlayerData objects of all online players...");
            dataManager.registerAllPlayers();
            MiscUtils.printToConsole("&7Running tasks...");
            runTasks();
        } else {
            MiscUtils.unloadPlugin("KauriLoader");
            MiscUtils.unloadPlugin("Atlas");
            MiscUtils.loadPlugin("Atlas");
            MiscUtils.loadPlugin("KauriLoader");
        }
        MiscUtils.printToConsole("&aCompleted reload in " + (System.currentTimeMillis() - start) + " milliseconds!");
    }

    private void registerListeners() {
        if(ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_12)) {
            getServer().getPluginManager().registerEvents(new LegacyListeners(), this);
        }
    }

    public void reloadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(UpdaterUtils.getPluginDirectory(), "messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(this.getResource("messages.yml"), "UTF8");
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            messages.setDefaults(defConfig);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getMessages() {
        if (messages == null) {
            reloadMessages();
        }
        return messages;
    }

    public void saveMessages() {
        if (messages == null || messagesFile == null) {
            return;
        }
        try {
            getMessages().save(messagesFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save messages file to " + messagesFile, ex);
        }
    }

    public void saveDefaultMessages() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            this.saveResource("messages.yml", false);
        }
    }
    //Credits: Luke.

    private void initializeScanner(Class<?> mainClass, Plugin plugin, boolean configOnly) {
        ClassScanner.scanFile(null, mainClass).stream().filter(c -> {
            try {
                Class clazz = Class.forName(c);

                return clazz.isAnnotationPresent(Init.class) || clazz.isAnnotationPresent(CheckInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).sorted(Comparator.comparingInt(c -> {
            try {
                Class clazz = Class.forName(c);

                if (clazz.isAnnotationPresent(Init.class)) {
                    Init annotation = (Init) clazz.getAnnotation(Init.class);

                    return annotation.priority().getPriority();
                } else {
                    return 3;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 3;
        })).forEachOrdered(c -> {
            try {
                Class clazz = Class.forName(c);

                Object obj = clazz.equals(this.getClass()) ? this : clazz.newInstance();

                if (clazz.isAnnotationPresent(Init.class)) {
                    Init init = (Init) clazz.getAnnotation(Init.class);

                    if (obj instanceof Listener) {
                        MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + " Bukkit listener. Registering...");
                        plugin.getServer().getPluginManager().registerEvents((Listener) obj, plugin);
                    } else if (obj instanceof cc.funkemunky.api.event.system.Listener) {
                        MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + "(deprecated) Atlas listener. Registering...");
                        cc.funkemunky.api.event.system.EventManager.register(plugin, (cc.funkemunky.api.event.system.Listener) obj);
                    } else if (obj instanceof AtlasListener) {
                        MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + "Atlas listener. Registering...");
                        Atlas.getInstance().getEventManager().registerListeners((AtlasListener) obj, plugin);
                    }

                    if (init.commands()) {
                        Atlas.getInstance().getCommandManager().registerCommands(plugin, obj);
                    }


                    Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getAnnotations().length > 0).forEach(field -> {
                        try {
                            field.setAccessible(true);
                            if (field.isAnnotationPresent(ConfigSetting.class)) {
                                String name = field.getAnnotation(ConfigSetting.class).name();
                                String path = field.getAnnotation(ConfigSetting.class).path() + "." + (name.length() > 0 ? name : field.getName());
                                try {
                                    MiscUtils.printToConsole("&eFound " + field.getName() + " ConfigSetting (default=" + field.get(obj) + ").");
                                    if (plugin.getConfig().get(path) == null) {
                                        MiscUtils.printToConsole("&eValue not found in configuration! Setting default into config...");
                                        plugin.getConfig().set(path, field.get(obj));
                                        plugin.saveConfig();
                                    } else {
                                        field.set(obj, plugin.getConfig().get(path));

                                        MiscUtils.printToConsole("&eValue found in configuration! Set value to &a" + plugin.getConfig().get(path));
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            } else if (!configOnly && field.isAnnotationPresent(Message.class)) {
                                Message msg = field.getAnnotation(Message.class);

                                MiscUtils.printToConsole("&eFound " + field.getName() + " Message (default=" + field.get(obj) + ").");
                                if (getMessages().get(msg.name()) != null) {
                                    MiscUtils.printToConsole("&eValue not found in message configuration! Setting default into messages.yml...");
                                    field.set(obj, getMessages().getString(msg.name()));
                                } else {
                                    getMessages().set(msg.name(), field.get(obj));
                                    saveMessages();
                                    MiscUtils.printToConsole("&eValue found in message configuration! Set value to &a" + plugin.getConfig().get(msg.name()));
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

                }

                if (clazz.isAnnotationPresent(CheckInfo.class)) {
                    getCheckManager().getCheckClasses().add(clazz);

                    MiscUtils.printToConsole("&eFound check &a" + ((CheckInfo) clazz.getAnnotation(CheckInfo.class)).name() + "&e! Registering...");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        getCheckManager().getCheckClasses().forEach(clazz -> getCheckManager().registerCheck(clazz, getCheckManager().getChecks()));
        MiscUtils.printToConsole("&aCompleted!");
    }
}
