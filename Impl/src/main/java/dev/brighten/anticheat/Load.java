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
import dev.brighten.anticheat.listeners.PacketListener;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Load {

    public static void load() {
        MiscUtils.printToConsole(Color.Gray + "Starting thread pool...");
        Kauri.INSTANCE.executor = Executors.newFixedThreadPool(3);
        if(PacketListener.packetThread.isShutdown())
            PacketListener.packetThread = Executors.newSingleThreadScheduledExecutor();

        MiscUtils.printToConsole(Color.Gray + "Loading config...");
        Kauri.INSTANCE.saveDefaultConfig();

        MiscUtils.printToConsole(Color.Gray + "Loading messages...");
        Kauri.INSTANCE.msgHandler = new MessageHandler(Kauri.INSTANCE);

        MiscUtils.printToConsole(Color.Gray + "Running scanner...");
        Atlas.getInstance().initializeScanner(Kauri.INSTANCE, true, true);

        MiscUtils.printToConsole(Color.Gray + "Setting the language to " + Color.Yellow + Config.language);
        Kauri.INSTANCE.msgHandler.setCurrentLang(Config.language);

        MiscUtils.printToConsole(Color.Gray + "Loading API...");
        Kauri.INSTANCE.kauriAPI = new KauriAPI();

        MiscUtils.printToConsole(Color.Gray + "Registering processors...");
        Kauri.INSTANCE.packetProcessor = new PacketProcessor();
        Kauri.INSTANCE.dataManager = new DataManager();
        Kauri.INSTANCE.loggerManager = new LoggerManager(true);
        EntityProcessor.start();

        MiscUtils.printToConsole(Color.Gray + "Registering checks...");
        Check.registerChecks();

        MiscUtils.printToConsole(Color.Gray + "Running tps task...");
        Kauri.INSTANCE.runTpsTask();
        Kauri.INSTANCE.profiler = new ToggleableProfiler();
        Kauri.INSTANCE.profiler.enabled = true;

        if(Bukkit.getOnlinePlayers().size() > 0) {
            RunUtils.taskLater(() -> {
                MiscUtils.printToConsole(Color.Gray + "Detected players! Creating data objects...");
                Bukkit.getOnlinePlayers().forEach(Kauri.INSTANCE.dataManager::createData);
            }, Kauri.INSTANCE, 6L);
        }
        Kauri.INSTANCE.lastEnabled = new TickTimer(20);
        Kauri.INSTANCE.enabled = true;
        Kauri.INSTANCE.lastEnabled.reset();

        Bukkit.getWorlds().forEach(world -> EntityProcessor.vehicles.put(world.getUID(), new ArrayList<>()));
    }
}
