package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.profiling.BaseProfiler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedClass;
import cc.funkemunky.api.tinyprotocol.api.packets.reflections.types.WrappedField;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.data.ObjectData;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Kauri extends JavaPlugin {

    public static Kauri INSTANCE;

    public PacketProcessor packetProcessor;
    public DataManager dataManager;

    //Lag Information
    public double tps;
    public TickTimer lastTickLag;

    public ExecutorService executor;
    public ToggleableProfiler profiler;

    public boolean enabled = false;

    public void onEnable() {
        MiscUtils.printToConsole(Color.Red + "Starting Kauri " + getDescription().getVersion() + "...");
        INSTANCE = this;

        load(); //Everything in one method so we can use it in other places like when reloading.
    }

    public void onDisable() {
        unload();
    }

    public void unload() {
        enabled = false;
        //Clearing all fields in ObjectData to prevent work from GC.
        MiscUtils.printToConsole("&7Shutting down threadPool and saving config...");
        saveConfig(); //Saving config.
        MiscUtils.printToConsole("&7Unregistering Atlas and Bukkit listeners...");
        HandlerList.unregisterAll(this); //Unregistering Bukkit listeners.
        Atlas.getInstance().getEventManager().unregisterAll(this); //Unregistering Atlas listeners.
        MiscUtils.printToConsole("&7Unregistering commands...");
        Atlas.getInstance().getCommandManager().unregisterCommand("kauri"); //Unregister all commands starting with the arg "Kauri".
        MiscUtils.printToConsole("&7Shutting down all Bukkit tasks...");
        Bukkit.getScheduler().cancelTasks(this); //Cancelling all Bukkit tasks for this plugin.

        MiscUtils.printToConsole("&7Collecting all fields to remove.");
        WrappedClass dataClass = new WrappedClass(ObjectData.class);
        List<WrappedField> fields = Arrays.stream(dataClass.getParent().getDeclaredFields())
                .map(field -> new WrappedField(dataClass, field))
                .collect(Collectors.toList());

        MiscUtils.printToConsole("&7Clearing all fields in all ObjectData classes (" + Kauri.INSTANCE.dataManager.dataMap.size() + ")...");
        //Looping through each data object to clear the stuff stored inside.
        Kauri.INSTANCE.dataManager.dataMap.keySet().forEach(key -> {
            ObjectData data = Kauri.INSTANCE.dataManager.dataMap.get(key);
            //Clearing checks.
            data.checkManager.checkMethods.clear();
            data.checkManager.checks.clear();

            //Just lets us go through every field automatically without having
            //to type it all manually.
            for (WrappedField field : fields) {
                if(!field.getType().equals(boolean.class) && !dev.brighten.anticheat.utils.MiscUtils.isReflectedAsNumber(field.getType())) {
                    field.set(data, null);
                }
            }
        });

        MiscUtils.printToConsole("&7Unloading DataManager...");
        //Clearing the dataManager.
        Kauri.INSTANCE.dataManager.dataMap.clear();
        Kauri.INSTANCE.dataManager.dataMap = null;
        Kauri.INSTANCE.dataManager = null;

        MiscUtils.printToConsole("&7Clearing checks and cached entity information...");
        EntityProcessor.vehicles.clear(); //Clearing all registered vehicles.
        //Clearing the checks.
        Check.checkClasses.clear();
        Check.checkSettings.clear();
        profiler.enabled = false;
        profiler = null;
        packetProcessor = null;
        executor.shutdown(); //Shutting down threads.
        INSTANCE = null;
    }

    public void load() {
        MiscUtils.printToConsole(Color.Gray + "Starting thread pool...");
        executor = Executors.newFixedThreadPool(3);

        MiscUtils.printToConsole(Color.Gray + "Loading config...");
        saveDefaultConfig();

        MiscUtils.printToConsole(Color.Gray + "Running scanner...");
        Atlas.getInstance().initializeScanner(getClass(), this, true, true);
        MiscUtils.printToConsole(Color.Gray + "Registering processors...");
        packetProcessor = new PacketProcessor();
        dataManager = new DataManager();
        EntityProcessor.start();

        MiscUtils.printToConsole(Color.Gray + "Registering checks...");
        Check.registerChecks();

        MiscUtils.printToConsole(Color.Gray + "Running tps task...");
        runTpsTask();
        profiler = new ToggleableProfiler();
        enabled = true;
    }

    private void runTpsTask() {
        lastTickLag = new TickTimer(6);
        new BukkitRunnable() {
            private int ticks;
            private long lastTimeStamp, lastTick;
            public void run() {
                ticks++;
                long currentTime = System.currentTimeMillis();

                if(currentTime - lastTick > 101) {
                    lastTickLag.reset();
                }
                if(ticks >= 10) {
                    ticks = 0;
                    tps = 500D / (currentTime - lastTimeStamp) * 20;
                    lastTimeStamp = currentTime;
                }
                lastTick = currentTime;
            }
        }.runTaskTimer(this, 1L, 1L);
    }
}
