package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.math.RollingAverageDouble;
import co.aikar.commands.BukkitCommandManager;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.commands.data.DataManager;
import dev.brighten.anticheat.commands.data.ObjectData;
import dev.brighten.anticheat.discord.DiscordAPI;
import dev.brighten.anticheat.listeners.api.EventHandler;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.processing.thread.ThreadHandler;
import dev.brighten.anticheat.utils.ServerInjector;
import dev.brighten.anticheat.utils.SystemUtil;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriAPI;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

public class Kauri extends JavaPlugin {

    public static Kauri INSTANCE;

    public PacketProcessor packetProcessor;
    public DataManager dataManager;
    public LoggerManager loggerManager;
    public KeepaliveProcessor keepaliveProcessor;
    public EventHandler eventHandler;
    public BukkitCommandManager commandManager;
    public ServerInjector injector;

    //Lag Information
    public RollingAverageDouble tps = new RollingAverageDouble(4, 20);
    public Timer lastTickLag;
    public long lastTick;
    public int currentTick;
    public ToggleableProfiler kauriProfiler;

    public ScheduledExecutorService loggingThread;
    public ExecutorService executor;
    public String LINK = "";

    public boolean enabled = false, usingPremium, usingAra;
    public Timer lastEnabled;

    public MessageHandler msgHandler;
    public KauriAPI kauriAPI;

    public boolean isNewer;
    public Metrics metrics;

    public Deque<Runnable> onReload = new LinkedList<>(), onTickEnd = new LinkedList<>();

    public void onEnable() {
        MiscUtils.printToConsole(Color.Red + "Starting Kauri " + getDescription().getVersion() + "...");
        INSTANCE = this;

        load(); //Everything in one method so we can use it in other places like when reloading.
    }

    public void onDisable() {
        unload(false);
    }

    public void unload(boolean reload) {
        enabled = reload;
        MiscUtils.printToConsole("&7Unloading DataManager...");
        //Clearing the dataManager.
        synchronized (dataManager.dataMap) {
            dataManager.dataMap.values().forEach(ObjectData::unregister);
            dataManager.dataMap.clear();
        }
        dataManager.hasAlerts.clear();
        dataManager.devAlerts.clear();
        dataManager = null;
        MiscUtils.printToConsole("&7Unregistering Kauri API...");
        kauriAPI.service.shutdown();

        MiscUtils.printToConsole("&7Unregistering processors...");
        keepaliveProcessor.stop();
        ThreadHandler.INSTANCE.shutdown();
        keepaliveProcessor = null;

        MiscUtils.printToConsole("&7Unregistering logging and database...");

        kauriAPI = null;
        MiscUtils.printToConsole("&7Unregistering Bukkit listeners...");
        HandlerList.unregisterAll(this); //Unregistering Bukkit listeners.
        MiscUtils.printToConsole("&7Unregistering commands...");
        //Unregister all commands starting with the arg "Kauri"
        commandManager.unregisterCommands();
        MiscUtils.printToConsole("&7Shutting down all Bukkit tasks...");
        Bukkit.getScheduler().cancelTasks(this); //Cancelling all Bukkit tasks for this plugin.

        MiscUtils.printToConsole("&7Unloading Discord Webhooks...");
        if(DiscordAPI.INSTANCE != null) DiscordAPI.INSTANCE.unload();
        DiscordAPI.INSTANCE = null;

        try {
            injector.eject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MiscUtils.printToConsole("&7Stopping log process...");
        loggerManager.storage.shutdown();
        loggerManager.storage = null;
        loggerManager = null;

        MiscUtils.printToConsole("&7Nullifying entries so plugin unloads from RAM completely...");
        //Clearing the checks.
        Check.checkClasses.clear();
        Check.checkSettings.clear();
        Atlas.getInstance().getPacketProcessor().removeListeners(Kauri.INSTANCE);
        if(!reload) {
            PacketProcessor.incomingPackets.clear();
            PacketProcessor.outgoingPackets.clear();
        }

        if(reload) {
            SystemUtil.CRC_32 = new CRC32();
        }

        packetProcessor = null;

        MiscUtils.printToConsole("&7Finshing up nullification...");
        msgHandler = null;
        onReload.clear();
        if(!reload)
        onReload = null;
        KauriAPI.INSTANCE.service.shutdown();
        KauriAPI.INSTANCE.dataManager = null;
        KauriAPI.INSTANCE.exemptHandler = null;
        KauriAPI.INSTANCE = null;
        if(!reload)
        tps = null;
        dev.brighten.anticheat.utils.MiscUtils.testers.clear();
        //Shutting down threads.
        executor.shutdown();
        loggingThread.shutdown();
        metrics = null;
        Atlas.getInstance().getBukkitCommandManagers().remove(Kauri.INSTANCE.getDescription().getName());
        commandManager = null;

        if(!reload)
            INSTANCE = null;
        MiscUtils.printToConsole("&aCompleted shutdown process.");
    }

    public void reload() {
        unload(true);
        load();
    }

    public void load() {
        isNewer = ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13);
        Load.load();
    }

    public TextComponent getMessage(String name, String def) {
        return new TextComponent(TextComponent.fromLegacyText(msgHandler.getLanguage().msg(name, def)));
    }

    public void runTpsTask() {
        lastTickLag = new AtlasTimer(6);
        AtomicInteger ticks = new AtomicInteger();
        AtomicLong lastTimeStamp = new AtomicLong(0);
        RunUtils.taskTimer(() -> {
            ticks.getAndIncrement();
            currentTick++;
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
    }

    public double getTps() {
        return tps.getAverage();
    }

    public void onReload(Runnable runnable) {
        onReload.add(runnable);
    }

    public void onTickEnd(Runnable runnable) {
        onTickEnd.add(runnable);
    }
}
