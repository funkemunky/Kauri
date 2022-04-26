package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.commands.CommandPropertiesManager;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.discord.DiscordAPI;
import dev.brighten.anticheat.listeners.api.EventHandler;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.utils.ServerInjector;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriAPI;
import org.bstats.bukkit.Metrics;

import java.util.concurrent.Executors;

public class Load {

    private static final int pluginId = 12727; // Kauri bStats plugin ID

    public static void load() {
        register("Kicking players online...");
        //Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Starting up..."));
        register("Starting thread pool...");
        Kauri.INSTANCE.executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("Kauri Misc Thread")
                .setUncaughtExceptionHandler((t, e) -> RunUtils.task(e::printStackTrace, Kauri.INSTANCE))
                .build());
        Kauri.INSTANCE.loggingThread = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder()
                .setNameFormat("Kauri Logging Threads")
                .setUncaughtExceptionHandler((t, e) -> RunUtils.task(e::printStackTrace, Kauri.INSTANCE))
                .build());

        register("Loading config...");
        Kauri.INSTANCE.saveDefaultConfig();

        register("Loading messages...");
        Kauri.INSTANCE.msgHandler = new MessageHandler(Kauri.INSTANCE);

        register("Registering processors...");
        Kauri.INSTANCE.dataManager = new DataManager();
        Kauri.INSTANCE.keepaliveProcessor = new KeepaliveProcessor();
        Kauri.INSTANCE.packetProcessor = new PacketProcessor();

        Kauri.INSTANCE.injector = new ServerInjector();

        Kauri.INSTANCE.kauriProfiler = new ToggleableProfiler();

        try {
            Kauri.INSTANCE.injector.inject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        register("Loading API...");
        Kauri.INSTANCE.kauriAPI = new KauriAPI();
        Kauri.INSTANCE.eventHandler = new EventHandler();

        register("Running scanner...");
        Atlas.getInstance().initializeScanner(Kauri.INSTANCE, true, true);

        new CommandPropertiesManager(Kauri.INSTANCE.commandManager, Kauri.INSTANCE.getDataFolder(),
                Kauri.INSTANCE.getResource("command-messages.properties"));

        register("Registering logging...");
        Kauri.INSTANCE.loggerManager = new LoggerManager();

        register("Registering checks...");
        Check.registerChecks();

        register("Setting the language to " + Color.Yellow + Config.language);
        Kauri.INSTANCE.msgHandler.setCurrentLang(Config.language);

        register("Discord Webhooks...");
        if (DiscordAPI.INSTANCE != null) DiscordAPI.INSTANCE.load();

        if (Config.metrics) {
            register("Loading metrics...");
            Kauri.INSTANCE.metrics = new Metrics(Kauri.INSTANCE, pluginId);
        }

        register("Running tps task...");
        Kauri.INSTANCE.runTpsTask();
        register("Starting profiler...");
        Kauri.INSTANCE.lastEnabled = new AtlasTimer(20);
        Kauri.INSTANCE.enabled = true;
        Kauri.INSTANCE.lastEnabled.reset();
    }

    private static void register(String string) {
        MiscUtils.printToConsole(Color.Gray + string);
    }
}
