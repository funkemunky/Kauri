package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Kauri extends JavaPlugin {

    public static Kauri INSTANCE;

    public PacketProcessor packetProcessor;
    public DataManager dataManager;
    public LoggerManager loggerManager;

    //Lag Information
    public double tps;
    public TickTimer lastTickLag;
    public long lastTick;

    public ExecutorService executor;
    public ToggleableProfiler profiler;

    public boolean enabled = false;
    public TickTimer lastEnabled;

    public MessageHandler msgHandler;
    public KauriAPI kauriAPI;

    public void onEnable() {
        MiscUtils.printToConsole(Color.Red + "Starting Kauri " + getDescription().getVersion() + "...");
        INSTANCE = this;

        load(); //Everything in one method so we can use it in other places like when reloading.
    }

    public void onDisable() {
        unload(false);
    }

    public void unload(boolean saveAsync) {
        enabled = false;
        MiscUtils.printToConsole("&7Saving logs to database...");
        loggerManager.save();
        MiscUtils.printToConsole("&7Unregistering Kauri API...");
        kauriAPI.service.shutdown();
        kauriAPI = null;

        MiscUtils.printToConsole("&7Unregistering Atlas and Bukkit listeners...");
        HandlerList.unregisterAll(this); //Unregistering Bukkit listeners.
        Atlas.getInstance().getEventManager().unregisterAll(this); //Unregistering Atlas listeners.
        MiscUtils.printToConsole("&7Unregistering commands...");
        //Unregister all commands starting with the arg "Kauri"
        Atlas.getInstance().getCommandManager().unregisterCommand("kauri");
        MiscUtils.printToConsole("&7Shutting down all Bukkit tasks...");
        Bukkit.getScheduler().cancelTasks(this); //Cancelling all Bukkit tasks for this plugin.
        MiscUtils.printToConsole("&7Unloading DataManager...");
        //Clearing the dataManager.
        Kauri.INSTANCE.dataManager.dataMap.clear();


        MiscUtils.printToConsole("&7Clearing checks and cached entity information...");
        EntityProcessor.vehicles.clear(); //Clearing all registered vehicles.
        //Clearing the checks.
        Check.checkClasses.clear();
        Check.checkSettings.clear();
        profiler.enabled = false;
        profiler = null;
        packetProcessor = null;
        loggerManager = null;
        executor.shutdown(); //Shutting down threads.
    }

    public void load() {
        MiscUtils.printToConsole(Color.Gray + "Starting thread pool...");
        executor = Executors.newFixedThreadPool(3);

        MiscUtils.printToConsole(Color.Gray + "Loading config...");
        saveDefaultConfig();

        MiscUtils.printToConsole(Color.Gray + "Loading messages...");
        msgHandler = new MessageHandler(this);

        MiscUtils.printToConsole(Color.Gray + "Running scanner...");
        Atlas.getInstance().initializeScanner(this, true, true);

        MiscUtils.printToConsole(Color.Gray + "Setting the language to " + Color.Yellow + Config.language);
        msgHandler.setCurrentLang(Config.language);

        MiscUtils.printToConsole(Color.Gray + "Loading API...");
        kauriAPI = new KauriAPI();

        MiscUtils.printToConsole(Color.Gray + "Registering processors...");
        packetProcessor = new PacketProcessor();
        dataManager = new DataManager();
        loggerManager = new LoggerManager(true);
        EntityProcessor.start();

        MiscUtils.printToConsole(Color.Gray + "Registering checks...");
        Check.registerChecks();

        MiscUtils.printToConsole(Color.Gray + "Running tps task...");
        runTpsTask();
        profiler = new ToggleableProfiler();
        profiler.enabled = true;

        if(Bukkit.getOnlinePlayers().size() > 0) {
            RunUtils.taskLater(() -> {
                MiscUtils.printToConsole(Color.Gray + "Detected players! Creating data objects...");
                Bukkit.getOnlinePlayers().forEach(dataManager::createData);
            }, this, 6L);
        }
        lastEnabled = new TickTimer(20);
        enabled = true;
        lastEnabled.reset();

        Bukkit.getWorlds().forEach(world -> EntityProcessor.vehicles.put(world.getUID(), new ArrayList<>()));
    }

    private void runTpsTask() {
        lastTickLag = new TickTimer(6);
        AtomicInteger ticks = new AtomicInteger();
        AtomicLong lastTimeStamp = new AtomicLong(0);
        RunUtils.taskTimer(() -> {
            ticks.getAndIncrement();
            long currentTime = System.currentTimeMillis();

            if(currentTime - lastTick > 120) {
                lastTickLag.reset();
            }
            if(ticks.get() >= 10) {
                ticks.set(0);
                tps = 500D / (currentTime - lastTimeStamp.get()) * 20;
                lastTimeStamp.set(currentTime);
            }
            lastTick = currentTime;
            Kauri.INSTANCE.lastTick = currentTime;
        }, this, 1L, 1L);
    }
}
