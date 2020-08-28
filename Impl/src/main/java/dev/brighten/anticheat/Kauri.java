package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.CommandManager;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.math.RollingAverageDouble;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.utils.TickTimer;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Kauri extends JavaPlugin {

    public static Kauri INSTANCE;

    public PacketProcessor packetProcessor;
    public DataManager dataManager;
    public LoggerManager loggerManager;
    public KeepaliveProcessor keepaliveProcessor;
    public EntityProcessor entityProcessor;

    //Lag Information
    public RollingAverageDouble tps = new RollingAverageDouble(4, 20);
    public TickTimer lastTickLag;
    public long lastTick;
    public CommandManager commandManager;

    public ExecutorService executor;
    public ScheduledExecutorService loggingThread;
    public ToggleableProfiler profiler;
    public String LINK = "";

    public boolean enabled = false;
    public TickTimer lastEnabled;

    public MessageHandler msgHandler;
    public KauriAPI kauriAPI;

    public boolean isNewer;

    public List<Runnable> onReload = new ArrayList<>();

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
        loggingThread.shutdown();

        MiscUtils.printToConsole("Unregistering processors...");
        keepaliveProcessor.stop();
        keepaliveProcessor = null;


        if(!reload) {
            kauriAPI = null;
            MiscUtils.printToConsole("&7Unregistering Atlas and Bukkit listeners...");
            HandlerList.unregisterAll(this); //Unregistering Bukkit listeners.
            Atlas.getInstance().getEventManager().unregisterAll(this); //Unregistering Atlas listeners.
            MiscUtils.printToConsole("&7Unregistering commands...");
            //Unregister all commands starting with the arg "Kauri"
            Atlas.getInstance().getCommandManager(Kauri.INSTANCE).unregisterCommands();
            Atlas.getInstance().getCommandManager(Kauri.INSTANCE).unregisterCommand("kauri");
            MiscUtils.printToConsole("&7Shutting down all Bukkit tasks...");
            Bukkit.getScheduler().cancelTasks(this); //Cancelling all Bukkit tasks for this plugin.
        }

        MiscUtils.printToConsole("&7Unloading DataManager...");
        //Clearing the dataManager.
        dataManager.dataMap.values().forEach(ObjectData::onLogout);
        dataManager.dataMap.clear();

        MiscUtils.printToConsole("&7Stopping log process...");
        loggerManager.storage.shutdown();
        loggerManager.storage = null;
        loggerManager = null;

        MiscUtils.printToConsole("&7Nullifying entries so plugin unloads from RAM completely...");
        //Clearing the checks.
        Check.checkClasses.clear();
        Check.checkSettings.clear();
        profiler.setEnabled(false);
        profiler = null;
        packetProcessor = null;

        MiscUtils.printToConsole("&7Clearing checks and cached entity information...");
        entityProcessor.vehicles.clear(); //Clearing all registered vehicles.
        entityProcessor.task.cancel();

        entityProcessor = null;

        MiscUtils.printToConsole("&7Finshing up nullification...");
        Atlas.getInstance().getPluginCommandManagers().remove(this.getName());
        msgHandler = null;
        dataManager = null;
        onReload.clear();
        onReload = null;
        executor.shutdown(); //Shutting down threads.

        INSTANCE = null;
        MiscUtils.printToConsole("&aCompleted shutdown process.");
    }

    public void reload() {
        Kauri.INSTANCE.reloadConfig();

        Check.checkClasses.clear();
        Check.checkSettings.clear();
        dataManager.dataMap.clear();
        entityProcessor.vehicles.clear();

        Atlas.getInstance().initializeScanner(this, false, false);

        loggerManager = new LoggerManager();

        for (Runnable runnable : onReload) {
            runnable.run();
            onReload.remove(runnable);
        }

        Bukkit.getOnlinePlayers().forEach(dataManager::createData);
    }

    public void load() {
        isNewer = ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13);
        Load.load();
    }

    public void runTpsTask() {
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
                tps.add(500D / (currentTime - lastTimeStamp.get()) * 20);
                lastTimeStamp.set(currentTime);
            }
            lastTick = currentTime;
            Kauri.INSTANCE.lastTick = currentTime;
        }, this, 1L, 1L);

        WrappedOutTransaction transaction =new WrappedOutTransaction(0, (short)69, false);
        RunUtils.taskTimerAsync(() ->
            Bukkit.getOnlinePlayers().forEach(player ->
                TinyProtocolHandler.sendPacket(player, transaction)), 40L, 40L);
    }

    public double getTps() {
        return tps.getAverage();
    }

    public void onReload(Runnable runnable) {
        onReload.add(runnable);
    }
}
