package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckManager;
import cc.funkemunky.anticheat.api.data.DataManager;
import cc.funkemunky.anticheat.api.data.banwave.BanwaveManager;
import cc.funkemunky.anticheat.api.data.logging.LoggerManager;
import cc.funkemunky.anticheat.api.data.stats.StatsManager;
import cc.funkemunky.anticheat.api.event.TickEvent;
import cc.funkemunky.anticheat.api.utils.Message;
import cc.funkemunky.anticheat.impl.commands.kauri.KauriCommand;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventManager;
import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.profiling.BaseProfiler;
import cc.funkemunky.api.updater.UpdaterUtils;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

@Getter
public class Kauri extends JavaPlugin {
    @Getter
    private static Kauri instance;

    private DataManager dataManager;
    private CheckManager checkManager;
    private StatsManager statsManager;
    private LoggerManager loggerManager;
    private BanwaveManager banwaveManager;

    private int currentTicks;
    private long lastTick, tickElapsed, profileStart;
    private double tps;

    private ScheduledExecutorService executorService, checkExecutor;

    private BaseProfiler profiler;

    private String requiredVersionOfAtlas = "1.2";
    private List<String> usableVersionsOfAtlas = Arrays.asList("1.1.4", "1.1.4.1", "1.2", "1.2-PRE-b4", "1.2-PRE-b5", "1.2-PRE-b6");

    private FileConfiguration messages;
    private File messagesFile;

    @Override
    public void onEnable() {
        //This allows us to access this class's contents from others places.
        instance = this;
        saveDefaultConfig();
        saveDefaultMessages();
        /*if (Bukkit.getPluginManager().getPlugin("KauriLoader") == null || !Bukkit.getPluginManager().getPlugin("KauriLoader").isEnabled())
            return;*/

        profiler = new BaseProfiler();
        profileStart = System.currentTimeMillis();

        dataManager = new DataManager();
        checkManager = new CheckManager();

        startScanner(false);

        dataManager.registerAllPlayers();

        //Starting up our utilities, managers, and tasks.

        statsManager = new StatsManager();
        loggerManager = new LoggerManager();
        loggerManager.loadFromDatabase();
        banwaveManager = new BanwaveManager();

        runTasks();
        registerCommands();

        executorService = Executors.newSingleThreadScheduledExecutor();
        checkExecutor = Executors.newScheduledThreadPool(2);

        //Registering all the commands
    }

    public void onDisable() {
        statsManager.saveStats();
        loggerManager.saveToDatabase();
        Atlas.getInstance().getEventManager().unregisterAll(this);
        EventManager.unregisterAll(this);
        org.bukkit.event.HandlerList.unregisterAll(this);
        Atlas.getInstance().getFunkeCommandManager().removeCommand("Kauri");
        dataManager.getDataObjects().clear();
        checkManager.getChecks().clear();
        executorService.shutdownNow();
        checkExecutor.shutdownNow();
    }

    private void runTasks() {
        //This allows us to use ticks for intervalTime comparisons to allow for more parrallel calculations to actual Minecraft
        //and it also has the added benefit of being lighter than using System.currentTimeMillis.
        new BukkitRunnable() {
            public void run() {
                TickEvent tickEvent = new TickEvent(currentTicks++);

                Atlas.getInstance().getEventManager().callEvent(tickEvent);
            }
        }.runTaskTimerAsynchronously(this, 1L, 1L);

        new BukkitRunnable() {
            public void run() {
                long timeStamp = System.currentTimeMillis();
                tickElapsed = timeStamp - lastTick;
                //Bukkit.broadcastMessage(tickElapsed + "ms" + ", " + getTPS());
                lastTick = timeStamp;
            }
        }.runTaskTimer(Kauri.getInstance(), 0L, 1L);

        new BukkitRunnable() {
            long sec;
            long currentSec;
            int ticks;

            public void run() {
                this.sec = (System.currentTimeMillis() / 1000L);
                if (this.currentSec == this.sec) {
                    this.ticks += 1;
                } else {
                    this.currentSec = this.sec;
                    Kauri.this.tps = (Kauri.this.tps == 0.0D ? this.ticks : (Kauri.this.tps + this.ticks) / 2.0D);
                    this.ticks = 0;
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    public void startScanner(boolean configOnly) {
        initializeScanner(getClass(), this, configOnly);
    }

    private void registerCommands() {
        Atlas.getInstance().getFunkeCommandManager().addCommand(new KauriCommand());
    }

    public double getTPSMS() {
        return 1000D / tickElapsed;
    }

    public double getTPS(RoundingMode mode, int places) {
        return MathUtils.round(1000D / tickElapsed, places, mode);
    }

    public void reloadKauri() {
        reloadConfig();
        reloadMessages();
        checkManager = new CheckManager();
        dataManager = new DataManager();
        HandlerList.unregisterAll(this);
        EventManager.unregisterAll(this);
        Atlas.getInstance().getEventManager().unregisterAll(this);
        startScanner(false);
        dataManager.registerAllPlayers();
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
                        Atlas.getInstance().getCommandManager().registerCommands(obj);
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
                            } else if (field.isAnnotationPresent(Message.class)) {
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
