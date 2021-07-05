package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.commands.ancmd.CommandManager;
import cc.funkemunky.api.config.MessageHandler;
import cc.funkemunky.api.profiling.ToggleableProfiler;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import cc.funkemunky.api.utils.RunUtils;
import dev.brighten.anticheat.check.api.Check;
import dev.brighten.anticheat.check.api.Config;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.discord.DiscordAPI;
import dev.brighten.anticheat.listeners.api.EventHandler;
import dev.brighten.anticheat.logs.LoggerManager;
import dev.brighten.anticheat.processing.EntityProcessor;
import dev.brighten.anticheat.processing.PacketProcessor;
import dev.brighten.anticheat.processing.keepalive.KeepaliveProcessor;
import dev.brighten.anticheat.utils.timer.impl.AtlasTimer;
import dev.brighten.api.KauriAPI;
import org.bukkit.Bukkit;

import java.util.ArrayList;
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
        Kauri.INSTANCE.dataManager = new DataManager();
        Kauri.INSTANCE.keepaliveProcessor = new KeepaliveProcessor();
        Kauri.INSTANCE.entityProcessor = EntityProcessor.start();
        Kauri.INSTANCE.packetProcessor = new PacketProcessor();

        register("Running scanner...");
        Atlas.getInstance().initializeScanner(Kauri.INSTANCE, true, true);

        register("Registering logging...");
        Kauri.INSTANCE.loggerManager = new LoggerManager();

        register("Setting the language to " + Color.Yellow + Config.language);
        Kauri.INSTANCE.msgHandler.setCurrentLang(Config.language);

        register("Registering checks...");
        Check.registerChecks();

        register("Discord Webhooks...");
        if(DiscordAPI.INSTANCE != null) DiscordAPI.INSTANCE.load();

        register("Running tps task...");
        Kauri.INSTANCE.runTpsTask();
        register("Starting profiler...");
        Kauri.INSTANCE.profiler = new ToggleableProfiler();
        Kauri.INSTANCE.profiler.setEnabled(true);
        Kauri.INSTANCE.lastEnabled = new AtlasTimer(20);
        Kauri.INSTANCE.enabled = true;
        Kauri.INSTANCE.lastEnabled.reset();

        Bukkit.getWorlds().forEach(world -> Kauri.INSTANCE.entityProcessor.vehicles.put(world.getUID(), new ArrayList<>()));
    }

    private static void register(String string) {
        MiscUtils.printToConsole(Color.Gray + string);
    }

}
