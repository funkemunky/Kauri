package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.CommandManager;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.CheckRegister;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.classloader.CheckLicense;
import dev.brighten.anticheat.classloader.KauriClassLoader;
import dev.brighten.anticheat.classloader.file.FileDownloader;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.listeners.api.EventHandler;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.utils.SystemUtil;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;

public class Load {

    public static void load() {
        register("Kicking players online...");
        //Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("Starting up..."));
        register("Starting thread pool...");
        Kauri.INSTANCE.executor = Executors.newScheduledThreadPool(3);
        Kauri.INSTANCE.loggingThread = Executors.newScheduledThreadPool(2);

        register("Loading config...");
        Kauri.INSTANCE.saveDefaultConfig();

        register("Loading API...");
        Kauri.INSTANCE.kauriAPI = new KauriAPI();
        Kauri.INSTANCE.eventHandler = new EventHandler();

        register("Loading commands...");
        Kauri.INSTANCE.commandManager = new CommandManager(Kauri.INSTANCE);

        register("Loading messages...");
        Kauri.INSTANCE.msgHandler = new MessageHandler(Kauri.INSTANCE);

        register("Registering processors...");
        Kauri.INSTANCE.packetProcessor = new PacketProcessor();
        Kauri.INSTANCE.dataManager = new DataManager();
        Kauri.INSTANCE.keepaliveProcessor = new KeepaliveProcessor();
        Kauri.INSTANCE.entityProcessor = EntityProcessor.start();

        register("Running scanner...");
        Atlas.getInstance().initializeScanner(Kauri.INSTANCE, true, true);

        register("Registering logging...");
        Kauri.INSTANCE.loggerManager = new LoggerManager();

        if(Config.initChecks) {
            register("Initializing checks...");
            Optional.ofNullable(Bukkit.getPluginManager().getPlugin("KauriLoader")).ifPresent(plugin -> {
                Config.license = plugin.getConfig().getString("license");
            });

            try {
                Kauri.INSTANCE.LINK = "https://funkemunky.cc/download?name=Kauri_New&license="
                        + URLEncoder.encode(Config.license, "UTF-8")
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
        Kauri.INSTANCE.lastEnabled = new AtlasTimer(20);
        Kauri.INSTANCE.enabled = true;
        Kauri.INSTANCE.lastEnabled.reset();

        Bukkit.getWorlds().forEach(world -> Kauri.INSTANCE.entityProcessor.vehicles.put(world.getUID(), new ArrayList<>()));

        CheckLicense.checkLicense();
    }

    private static void register(String string) {
        MiscUtils.printToConsole(Color.Gray + string);
    }

    public static void startClassLoader() {
        //don't fucking modify or i will snap ur neck
        for (int i = 0; i < 100; i++) {
            SystemUtil.CRC_32.update(("GzB@aRC1$^JEKQxGmSBAQ%%WohM7LZnuC*pVhf0%B6VyZMyOvU" + i)
                    .getBytes(StandardCharsets.UTF_8));
        }

        loadVersion(Kauri.INSTANCE.LINK);
    }

    private static void loadVersion(String url) {

        FileDownloader fileDownloader = new FileDownloader(url);
        File downloadedFile = fileDownloader.download();

        if (downloadedFile.exists()) {
            try {
                KauriClassLoader kauriClassLoader = new KauriClassLoader(downloadedFile.toURI().toURL(),
                        Kauri.INSTANCE.getClass().getClassLoader());

                if(kauriClassLoader.getClassBytes() != null) {
                    kauriClassLoader.getClassBytes()
                            .forEach((key, bytes) -> {
                                Class<?> claz = kauriClassLoader.loadClass(key);
                                if(CheckRegister.class.isAssignableFrom(claz)) {
                                    try {
                                        claz.newInstance();
                                    } catch (InstantiationException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                    });
                }
                /*Optional.ofNullable(kauriClassLoader.loadClass(free)).ifPresent(clazz -> {
                    try {
                        clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                Optional.ofNullable(kauriClassLoader.loadClass(regular)).ifPresent(clazz -> {
                    try {
                        clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                Optional.ofNullable(kauriClassLoader.loadClass(premium)).ifPresent(clazz -> {
                    try {
                        clazz.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });*/
                System.out.println("Deleting");
                while(!downloadedFile.delete() && downloadedFile.exists());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
