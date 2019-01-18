package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.api.checks.CheckManager;
import cc.funkemunky.anticheat.api.data.DataManager;
import cc.funkemunky.anticheat.api.utils.ConfigSetting;
import cc.funkemunky.anticheat.impl.commands.kauri.KauriCommand;
import cc.funkemunky.anticheat.impl.listeners.BukkitListeners;
import cc.funkemunky.anticheat.impl.listeners.PacketListeners;
import cc.funkemunky.anticheat.impl.listeners.PlayerConnectionListeners;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.event.system.EventManager;
import cc.funkemunky.api.utils.ClassScanner;
import cc.funkemunky.api.utils.MiscUtils;
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
    private int currentTicks;
    private long serverSpeed, timestamp;
    private ScheduledExecutorService executorService;

    @Override
    public void onEnable() {
        //This allows us to access this class's contents from others places.
        instance = this;
        saveDefaultConfig();

        //Starting up our utilities, managers, and tasks.
        checkManager = new CheckManager();
        dataManager = new DataManager();
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
        Kauri.getInstance().getDataManager().getDataObjects().clear();
    }

    private void runTasks() {
        //This allows us to use ticks for time comparisons to allow for more parrallel calculations to actual Minecraft
        //and it also has the added benefit of being lighter than using System.currentTimeMillis.
        new BukkitRunnable() {
            public void run() {
                currentTicks++;

                val now = System.currentTimeMillis();

                val difference = now - timestamp;

                serverSpeed = difference;
                timestamp = now;
            }
        }.runTaskTimerAsynchronously(this, 0L, 1L);
    }

    private void registerCommands() {
        Atlas.getInstance().getFunkeCommandManager().addCommand(new KauriCommand());
    }

    private void startScanner() {
        ClassScanner.scanFile(null, getClass()).forEach(c -> {
            try {
                Class clazz = Class.forName(c);
                Object obj = clazz.newInstance();

                Bukkit.broadcastMessage(clazz.getName());
                if (obj instanceof Listener) {
                    MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + " Bukkit listener. Registering...");
                    Bukkit.getPluginManager().registerEvents((Listener) obj, this);
                } else if (obj instanceof cc.funkemunky.api.event.system.Listener) {
                    MiscUtils.printToConsole("&eFound " + clazz.getSimpleName() + " Atlas listener. Registering...");
                    EventManager.register((cc.funkemunky.api.event.system.Listener) obj);
                }

                Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(ConfigSetting.class)).forEach(field -> {
                    String path = field.getAnnotation(ConfigSetting.class).path() + "." + field.getName();
                    try {
                        field.setAccessible(true);
                        MiscUtils.printToConsole("&eFound " + field.getName() + " ConfigSetting (default=" + field.get(obj) + ").");
                        if (getConfig().get(path) == null) {
                            MiscUtils.printToConsole("&eValue not found in configuration! Setting default into config...");
                            getConfig().set(path, field.get(obj));
                            saveConfig();
                        } else {
                            field.set(obj, getConfig().get(path));
                            MiscUtils.printToConsole("&eValue found in configuration! Set value to &a" + getConfig().get(path));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
