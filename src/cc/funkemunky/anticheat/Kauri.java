package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.api.checks.CheckManager;
import cc.funkemunky.anticheat.api.data.DataManager;
import cc.funkemunky.anticheat.api.events.TickEvent;
import cc.funkemunky.anticheat.api.log.LoggerManager;
import cc.funkemunky.anticheat.api.mongo.Mongo;
import cc.funkemunky.anticheat.impl.commands.kauri.KauriCommand;
import cc.funkemunky.anticheat.impl.listeners.BukkitListeners;
import cc.funkemunky.anticheat.impl.listeners.PacketListeners;
import cc.funkemunky.anticheat.impl.listeners.PlayerConnectionListeners;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventManager;
import cc.funkemunky.api.utils.*;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class Kauri extends JavaPlugin {
    @Getter
    private static Kauri instance;
    private DataManager dataManager;
    private CheckManager checkManager;
    private LoggerManager loggerManager;
    private Mongo mongo;
    private int currentTicks;
    private long serverSpeed, lastTick;
    private ScheduledExecutorService executorService;

    @Override
    public void onEnable() {
        //This allows us to access this class's contents from others places.
        instance = this;
        saveDefaultConfig();

        startScanner();

        //Starting up our utilities, managers, and tasks.
        checkManager = new CheckManager();
        checkManager.init();
        dataManager = new DataManager();
        mongo = new Mongo();

        mongo.connect();

        loggerManager = new LoggerManager();
        runTasks();

        executorService = Executors.newSingleThreadScheduledExecutor();
        //Registering all the listeners to Bukkit's event handler.
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListeners(), this);
        Bukkit.getPluginManager().registerEvents(new BukkitListeners(), this);

        //Register all the Atlas listeners to Atlas's event handler.
        EventManager.register(new PacketListeners());

        //Registering all the commands
        registerCommands();
    }

    public void onDisable() {
        EventManager.clearRegistered();
        org.bukkit.event.HandlerList.unregisterAll(this);
        Atlas.getInstance().getFunkeCommandManager().removeCommand("kauri");
        Kauri.getInstance().getDataManager().getDataObjects().clear();
        getServer().getScheduler().cancelTasks(this);
    }

    private void runTasks() {
        //This allows us to use ticks for time comparisons to allow for more parrallel calculations to actual Minecraft
        //and it also has the added benefit of being lighter than using System.currentTimeMillis.
        new BukkitRunnable() {
            public void run() {
                TickEvent tickEvent = new TickEvent(currentTicks++);

                EventManager.callEvent(tickEvent);
            }
        }.runTaskTimer(this, 0L, 1L);

        new BukkitRunnable() {
            public void run() {
                serverSpeed = MathUtils.elapsed(lastTick) / 2;
                lastTick = System.currentTimeMillis();
            }
        }.runTaskTimer(this, 1L, 2L);
    }

    private void registerCommands() {
        Atlas.getInstance().getFunkeCommandManager().addCommand(new KauriCommand());
    }

    private void startScanner() {
        Atlas.getInstance().initializeScanner(getClass(), this);
    }

    public double getTPS() {
        return 1000D / serverSpeed;
    }
}
