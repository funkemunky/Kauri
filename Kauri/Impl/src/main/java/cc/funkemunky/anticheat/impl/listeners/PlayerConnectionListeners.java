package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.impl.menu.InputHandler;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Init
public class PlayerConnectionListeners implements Listener {

    @ConfigSetting(path = "data.logging", name = "removeBanInfoOnJoin")
    private static boolean removeBanOnJoin = true;
    private static AtomicLong lastCheck = new AtomicLong(System.currentTimeMillis());
    private static String website = "https://funkemunky.cc/download/verify?license=%license%&downloader=Kauri";

    @ConfigSetting(path = "misc", name = "inventoryChecking")
    static boolean inventoryChecking = true;

    private static ScheduledFuture task = Kauri.getInstance().getExecutorService().scheduleAtFixedRate(() -> {
        String license = Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license");
        try {
            URL url = new URL(website.replaceAll("%license%", URLEncoder.encode(license, "UTF-8")));
            URLConnection connection = url.openConnection();

            ObjectInputStream stream = new ObjectInputStream(connection.getInputStream());

            if(!Boolean.parseBoolean(stream.readUTF()) && InputHandler.testMode == -69) {
                Bukkit.getPluginManager().disablePlugin(Kauri.getInstance());
                MiscUtils.printToConsole("&cPlugin disabled due to piracy. &7&oPlease contact support @ &a&ohttps://funkeunky.cc/contact &7&oif this is in error.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            //TODO Remove this print trace once tested and replace with plugin disabler and message.
        }
    }, 20L, 10L, TimeUnit.SECONDS);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Kauri.getInstance().getExecutorService().execute(() -> {
            Kauri.getInstance().getDataManager().addData(event.getPlayer().getUniqueId());
            if (removeBanOnJoin && Kauri.getInstance().getLoggerManager().isBanned(event.getPlayer().getUniqueId())) {
                Kauri.getInstance().getLoggerManager().removeBan(event.getPlayer().getUniqueId());
            }

            //TODO Ensure this is only running when task is cancelled and Im doing this right.
            if(task.isCancelled() && System.currentTimeMillis() - lastCheck.get() > 10000L) {
                String license = Bukkit.getPluginManager().getPlugin("KauriLoader").getConfig().getString("license");
                try {
                    URL url = new URL(website.replaceAll("%license%", URLEncoder.encode(license, "UTF-8")));
                    URLConnection connection = url.openConnection();

                    ObjectInputStream stream = new ObjectInputStream(connection.getInputStream());

                    if(!Boolean.parseBoolean(stream.readUTF()) && InputHandler.testMode == -69) {
                        Bukkit.getPluginManager().disablePlugin(Kauri.getInstance());
                        MiscUtils.printToConsole("&cPlugin disabled due to piracy. &7&oPlease contact support @ &a&ohttps://funkeunky.cc/contact &7&oif this is in error.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //TODO Remove this print trace once tested and replace with plugin disabler and message.
                }
            }
        });

        if (event.getPlayer().getName().equals("funkemunky")) {
            event.getPlayer().sendMessage(Color.Gray + "This server is using Kauri " + Kauri.getInstance().getDescription().getVersion());
        }

        if (inventoryChecking && ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9))
            event.getPlayer().removeAchievement(Achievement.OPEN_INVENTORY);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Kauri.getInstance().getDataManager().removeData(event.getPlayer().getUniqueId());
    }
}
