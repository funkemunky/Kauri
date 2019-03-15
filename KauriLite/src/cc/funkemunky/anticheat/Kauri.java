package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.api.checks.CheckManager;
import cc.funkemunky.anticheat.api.data.DataManager;
import cc.funkemunky.anticheat.api.data.logging.LoggerManager;
import cc.funkemunky.anticheat.api.data.stats.StatsManager;
import cc.funkemunky.anticheat.api.event.TickEvent;
import cc.funkemunky.anticheat.impl.commands.kauri.KauriCommand;
import cc.funkemunky.anticheat.impl.listeners.FunkeListeners;
import cc.funkemunky.anticheat.impl.listeners.PacketListeners;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventManager;
import cc.funkemunky.api.profiling.BaseProfiler;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
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
    private int currentTicks;
    private long lastTick, tickElapsed, profileStart;
    private ScheduledExecutorService executorService;
    private BaseProfiler profiler;
    private LoggerManager loggerManager;

    private String requiredVersionOfAtlas = "1.1.3.4";
    private List<String> usableVersionsOfAtlas = Arrays.asList("1.1.3.4", "1.1.3.3");

    @Override
    public void onEnable() {
        //This allows us to access this class's contents from others places.
        instance = this;
        saveDefaultConfig();

        //if(Bukkit.getPluginManager().getPlugin("KauriLoader") == null || !Bukkit.getPluginManager().getPlugin("KauriLoader").isEnabled()) return;

        if(Bukkit.getPluginManager().isPluginEnabled("Atlas") && usableVersionsOfAtlas.contains(Bukkit.getPluginManager().getPlugin("Atlas").getDescription().getVersion())) {

            profiler = new BaseProfiler();
            profileStart = System.currentTimeMillis();

            dataManager = new DataManager();

            startScanner(false);

            checkManager = new CheckManager();
            dataManager.registerAllPlayers();

            //Starting up our utilities, managers, and tasks.

            statsManager = new StatsManager();
            loggerManager = new LoggerManager();
            loggerManager.loadFromDatabase();

            runTasks();
            registerCommands();

            MiscUtils.printToConsole("&aSuccessfully loaded Kauri and all of its libraries!");
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "You do not the required Atlas dependency installed! Try restarting the server to see if the downloader will do it properly next time.");
        }

        executorService = Executors.newSingleThreadScheduledExecutor();

        //Registering all the commands
    }


    public void onDisable() {
        statsManager.saveStats();
        loggerManager.saveToDatabase();
        EventManager.unregister(new FunkeListeners());
        EventManager.unregister(new PacketListeners());
        org.bukkit.event.HandlerList.unregisterAll(this);
        dataManager.getDataObjects().clear();
        checkManager.getChecks().clear();
        executorService.shutdownNow();
    }

    private void runTasks() {
        //This allows us to use ticks for time comparisons to allow for more parrallel calculations to actual Minecraft
        //and it also has the added benefit of being lighter than using System.currentTimeMillis.
        new BukkitRunnable() {
            public void run() {
                TickEvent tickEvent = new TickEvent(currentTicks++);

                EventManager.callEvent(tickEvent);
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
    }

    public void startScanner(boolean configOnly) {
        initializeScanner(getClass(), this, configOnly);
    }

    private void registerCommands() {
        Atlas.getInstance().getFunkeCommandManager().addCommand(new KauriCommand());
    }

    public double getTPS() {
        return 1000D / tickElapsed;
    }

    public double getTPS(RoundingMode mode, int places) {
        return MathUtils.round(1000D / tickElapsed, places, mode);
    }

    public void reloadKauri() {
        reloadConfig();
        getCheckManager().getChecks().clear();
        getDataManager().getDataObjects().clear();
        startScanner(true);
        checkManager = new CheckManager();
        dataManager = new DataManager();
    }

    //Credits: Luke.

    private void initializeScanner(Class<?> mainClass, Plugin plugin, boolean configOnly) {
        ClassScanner.scanFile(null, mainClass).stream().filter(c -> {
            try {
                Class clazz = Class.forName(c);

                return clazz.isAnnotationPresent(Init.class);
            } catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }).sorted(Comparator.comparingInt(c -> {
            try {
                Class clazz = Class.forName(c);

                Init annotation = (Init) clazz.getAnnotation(Init.class);

                return annotation.priority().getPriority();
            } catch(Exception e) {
                e.printStackTrace();
            }
            return 3;
        })).forEachOrdered(c -> {
            try {
                Class clazz = Class.forName(c);

                if(clazz.isAnnotationPresent(Init.class)) {
                    Object obj = clazz.getSimpleName().equals(mainClass.getSimpleName()) ? plugin : clazz.newInstance();
                    Init init = (Init) clazz.getAnnotation(Init.class);

                    if(!configOnly) {
                        if (obj instanceof Listener) {
                            MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + " Bukkit listener. Registering...");
                            Bukkit.getPluginManager().registerEvents((Listener) obj, plugin);
                        } else if(obj instanceof cc.funkemunky.api.event.system.Listener) {
                            MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + " Atlas listener. Registering...");
                            EventManager.register(plugin, (cc.funkemunky.api.event.system.Listener) obj);
                        }

                        if(init.commands()) {
                            Atlas.getInstance().getCommandManager().registerCommands(obj);
                        }

                    }
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        if(field.isAnnotationPresent(ConfigSetting.class)) {
                            String name = field.getAnnotation(ConfigSetting.class).name();
                            String path = field.getAnnotation(ConfigSetting.class).path() + "." + (name.length() > 0 ? name : field.getName());
                            try {
                                MiscUtils.printToConsole("&eFound " + field.getName() + " ConfigSetting (default=" + field.get(obj) + ").");
                                if(plugin.getConfig().get(path) == null) {
                                    MiscUtils.printToConsole("&eValue not found in configuration! Setting default into config...");
                                    plugin.getConfig().set(path, field.get(obj));
                                    plugin.saveConfig();
                                } else {
                                    field.set(obj, plugin.getConfig().get(path));

                                    MiscUtils.printToConsole("&eValue found in configuration! Set value to &a" + plugin.getConfig().get(path));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
