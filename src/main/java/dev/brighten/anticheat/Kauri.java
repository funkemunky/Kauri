package dev.brighten.anticheat;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.MiscUtils;
import com.sun.corba.se.impl.orb.DataCollectorBase;
import dev.brighten.anticheat.api.check.Check;
import dev.brighten.anticheat.data.DataManager;
import dev.brighten.anticheat.processing.PacketProcessor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Kauri extends JavaPlugin {

    public static Kauri INSTANCE;

    public PacketProcessor packetProcessor;
    public DataManager dataManager;

    public double tps;

    public ExecutorService executor;

    public void onEnable() {
        MiscUtils.printToConsole(Color.Red + "Starting Kauri " + getDescription().getVersion() + "...");
        INSTANCE = this;
        executor = Executors.newFixedThreadPool(2);

        MiscUtils.printToConsole(Color.Gray + "Running scanner...");
        Atlas.getInstance().initializeScanner(getClass(), this, true, true);
        MiscUtils.printToConsole(Color.Gray + "Registering processors...");
        packetProcessor = new PacketProcessor();
        dataManager = new DataManager();

        MiscUtils.printToConsole(Color.Gray + "Registering checks...");
        Check.registerChecks();
    }

    public void onDisable() {

    }
}
