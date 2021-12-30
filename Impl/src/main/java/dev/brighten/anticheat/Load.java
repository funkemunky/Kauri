package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import cc.funkemunky.api.utils.com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckInfo;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.classloader.KauriClassLoader;
import dev.brighten.anticheat.classloader.file.FileDownloader;
import dev.brighten.anticheat.commands.CommandPropertiesManager;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.discord.DiscordAPI;
import dev.brighten.anticheat.listeners.api.EventHandler;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.utils.ServerInjector;
import dev.brighten.anticheat.utils.SystemUtil;
import dev.brighten.anticheat.utils.ThreadHandler;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriAPI;
import dev.brighten.api.KauriVersion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executors;

public class Load {

    private static final int pluginId = 12727; // Kauri bStats plugin ID

    public static void load() {
        register("Kicking players online...");
        //Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Starting up..."));
        register("Starting thread pool...");
        Kauri.INSTANCE.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("Kauri Threads")
                .setUncaughtExceptionHandler((t, e) -> RunUtils.task(e::printStackTrace, Kauri.INSTANCE))
                .build());
        Kauri.INSTANCE.loggingThread = Executors.newSingleThreadScheduledExecutor();

        Bukkit.getOnlinePlayers().forEach(ThreadHandler::addPlayer);

        register("Loading config...");
        Atlas.getInstance().registerConfig(Kauri.INSTANCE);

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

        if(!SystemUtil.license.equals("Insert Kauri Ara license here")) {
            register("Initializing checks...");
            try {
                Kauri.INSTANCE.LINK = "https://funkemunky.cc/download?name=Kauri_New&license="
                        + URLEncoder.encode(SystemUtil.license, "UTF-8")
                        + "&version=" + URLEncoder.encode(Kauri.INSTANCE.getDescription().getVersion(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            startClassLoader();
        }

        register("Setting the language to " + Color.Yellow + Config.language);
        Kauri.INSTANCE.msgHandler.setCurrentLang(Config.language);

        register("Registering checks...");
        Check.registerChecks();

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

        //Creating data for online players as of now. We're running this sync so it only runs once server fully starts.
        RunUtils.taskLater(() -> Bukkit.getOnlinePlayers().forEach(pl -> Kauri.INSTANCE.dataManager.createData(pl)),
                Kauri.INSTANCE, 1);
    }

    private static void register(String string) {
        MiscUtils.printToConsole(Color.Gray + string);
    }

    public static void startClassLoader() {
        //don't fucking modify or i will snap ur neck
        for (int i = 0; i < 100; i++) {
            SystemUtil.CRC_32.update(("GzB@aRC1$^JEKQxGmSBAQ%%WohM7LZnuC*pVhf0%B6VyZMyOvU" + i).getBytes(StandardCharsets.UTF_8));
        }

        loadVersion(Kauri.INSTANCE.LINK);
    }



    private static void loadVersion(String url) {

        FileDownloader fileDownloader = new FileDownloader(url);
        File downloadedFile = fileDownloader.download();

        if (downloadedFile.exists())
            try {
                KauriClassLoader kauriClassLoader = new KauriClassLoader(downloadedFile.toURI().toURL(), Kauri.INSTANCE.getClass().getClassLoader());

                kauriClassLoader.getClassBytes().forEach((key, bits) -> {
                    if(key.endsWith(".")) return;

                    Class<?> clazz = kauriClassLoader.loadClass(key);

                    if(clazz.isAnnotationPresent(CheckInfo.class) && clazz.getAnnotation(CheckInfo.class).planVersion() == KauriVersion.ARA) {
                        Check.register(clazz);
                    } else if(clazz.isAssignableFrom(CheckRegister.class)) {
                        try {
                            clazz.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
                downloadedFile.delete();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
    }
}
