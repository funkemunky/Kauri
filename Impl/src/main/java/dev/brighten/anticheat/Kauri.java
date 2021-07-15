package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.math.RollingAverageDouble;
import co.aikar.commands.BukkitCommandManager;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.discord.DiscordAPI;
import dev.brighten.anticheat.listeners.api.EventHandler;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.utils.StringUtils;
import dev.brighten.anticheat.utils.timer.Timer;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriAPI;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

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
    public EventHandler eventHandler;
    public BukkitCommandManager commandManager;

    //Lag Information
    public RollingAverageDouble tps = new RollingAverageDouble(4, 20);
    public Timer lastTickLag;
    public long lastTick;

    public ScheduledExecutorService loggingThread;
    public ExecutorService executor;
    public String LINK = "";

    public boolean enabled = false, usingPremium, usingAra;
    public Timer lastEnabled;

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
        Atlas.getInstance().getPacketProcessor().removeListeners(this);

        if(!reload) {
            kauriAPI = null;
            MiscUtils.printToConsole("&7Unregistering Atlas and Bukkit listeners...");
            HandlerList.unregisterAll(this); //Unregistering Bukkit listeners.
            Atlas.getInstance().getEventManager().unregisterAll(this); //Unregistering Atlas listeners.
            MiscUtils.printToConsole("&7Unregistering commands...");
            //Unregister all commands starting with the arg "Kauri"
            commandManager.unregisterCommands();
            MiscUtils.printToConsole("&7Shutting down all Bukkit tasks...");
            Bukkit.getScheduler().cancelTasks(this); //Cancelling all Bukkit tasks for this plugin.
        }

        MiscUtils.printToConsole("&7Unloading Discord Webhooks...");
        if(DiscordAPI.INSTANCE != null) DiscordAPI.INSTANCE.unload();

        MiscUtils.printToConsole("&7Unloading DataManager...");
        //Clearing the dataManager.
        dataManager.dataMap.values().forEach(ObjectData::unregister);
        dataManager.dataMap.clear();

        MiscUtils.printToConsole("&7Stopping log process...");
        loggerManager.storage.shutdown();
        loggerManager.storage = null;
        loggerManager = null;

        MiscUtils.printToConsole("&7Nullifying entries so plugin unloads from RAM completely...");
        //Clearing the checks.
        Check.checkClasses.clear();
        Check.checkSettings.clear();
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
        loggerManager.storage.shutdown();
        commandManager.unregisterCommands();

        Atlas.getInstance().initializeScanner(this, false, false);

        StringUtils.Messages.reload();

        loggerManager = new LoggerManager();

        for (Player player : Bukkit.getOnlinePlayers()) {
            dataManager.createData(player);
        }

        for (Runnable runnable : onReload) {
            runnable.run();
            onReload.remove(runnable);
        }
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
}
