package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.CommandManager;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.utils.TickTimer;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Load {

    public static void load() {
        register("Kicking players online...");
        //Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Starting up..."));
        register("Starting thread pool...");
        Kauri.INSTANCE.executor = Executors.newFixedThreadPool(3);
        Kauri.INSTANCE.loggingThread = Executors.newScheduledThreadPool(2);

        register("Loading config...");
        Kauri.INSTANCE.saveDefaultConfig();

        register("Loading commands...");
        Kauri.INSTANCE.commandManager = new CommandManager(Kauri.INSTANCE);

        register("Loading messages...");
        Kauri.INSTANCE.msgHandler = new MessageHandler(Kauri.INSTANCE);

        register("Running scanner...");
        Atlas.getInstance().initializeScanner(Kauri.INSTANCE, true, true);

        register("Setting the language to " + Color.Yellow + Config.language);
        Kauri.INSTANCE.msgHandler.setCurrentLang(Config.language);

        register("Loading API...");
        Kauri.INSTANCE.kauriAPI = new KauriAPI();

        register("Registering processors...");
        Kauri.INSTANCE.packetProcessor = new PacketProcessor();
        Kauri.INSTANCE.dataManager = new DataManager();
        Kauri.INSTANCE.loggerManager = new LoggerManager();
        Kauri.INSTANCE.keepaliveProcessor = new KeepaliveProcessor();
        Kauri.INSTANCE.entityProcessor = new EntityProcessor().start();

        register("Registering checks...");
        Check.registerChecks();

        register("Running tps task...");
        Kauri.INSTANCE.runTpsTask();
        register("Starting profiler...");
        Kauri.INSTANCE.profiler = new ToggleableProfiler();
        Kauri.INSTANCE.profiler.setEnabled(true);

        if(Bukkit.getOnlinePlayers().size() > 0) {
            RunUtils.taskLater(() -> {
                MiscUtils.printToConsole(Color.Gray + "Detected players! Creating data objects...");
                Bukkit.getOnlinePlayers().forEach(Kauri.INSTANCE.dataManager::createData);
            }, Kauri.INSTANCE, 6L);
        }
        Kauri.INSTANCE.lastEnabled = new TickTimer(20);
        Kauri.INSTANCE.enabled = true;
        Kauri.INSTANCE.lastEnabled.reset();

        Bukkit.getWorlds().forEach(world -> Kauri.INSTANCE.entityProcessor.vehicles.put(world.getUID(), new ArrayList<>()));
    }

    private static void register(String string) {
        MiscUtils.printToConsole(Color.Gray + string);
    }
}
