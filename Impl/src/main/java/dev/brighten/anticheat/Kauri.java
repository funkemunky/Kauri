package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.listeners.PacketListener;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
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

    public boolean isNewer;

    public void onEnable() {
        MiscUtils.printToConsole(Color.Red + "Starting Kauri " + getDescription().getVersion() + "...");
        INSTANCE = this;

        load(); //Everything in one method so we can use it in other places like when reloading.
    }

    public void onDisable() {
        unload(false);
    }

    public void unload(boolean reload) {
        reload = false;
        enabled = reload;
        MiscUtils.printToConsole("&7Unregistering Kauri API...");
        kauriAPI.service.shutdown();

        PacketListener.packetThread.shutdown();

        if(!reload) {
            kauriAPI = null;
            MiscUtils.printToConsole("&7Unregistering Atlas and Bukkit listeners...");
            HandlerList.unregisterAll(this); //Unregistering Bukkit listeners.
            Atlas.getInstance().getEventManager().unregisterAll(this); //Unregistering Atlas listeners.
            MiscUtils.printToConsole("&7Unregistering commands...");
            //Unregister all commands starting with the arg "Kauri"
            Atlas.getInstance().getCommandManager().unregisterCommand("kauri");
            MiscUtils.printToConsole("&7Shutting down all Bukkit tasks...");
            Bukkit.getScheduler().cancelTasks(this); //Cancelling all Bukkit tasks for this plugin.
        }

        MiscUtils.printToConsole("&7Unloading DataManager...");
        //Clearing the dataManager.
        Kauri.INSTANCE.dataManager.dataMap.clear();


        MiscUtils.printToConsole("&7Clearing checks and cached entity information...");
        EntityProcessor.vehicles.clear(); //Clearing all registered vehicles.
        //Clearing the checks.
        Check.checkClasses.clear();
        Check.checkSettings.clear();
        if(!reload) {
            profiler.setEnabled(false);
            profiler = null;
            packetProcessor = null;
            loggerManager = null;
        }
        executor.shutdown(); //Shutting down threads.
    }

    public void reload() {
        Kauri.INSTANCE.reloadConfig();
        Atlas.getInstance().initializeScanner(this, false, false);

        dataManager.dataMap.clear();
        EntityProcessor.vehicles.clear();

        Check.checkClasses.clear();
        Check.checkSettings.clear();
        Check.registerChecks();

        loggerManager = new LoggerManager(true);

        Bukkit.getOnlinePlayers().forEach(dataManager::createData);
    }

    public void load() {
        isNewer = ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13);
        Load.load();
    }

    void runTpsTask() {
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
