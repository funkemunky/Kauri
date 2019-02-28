package cc.funkemunky.kauri.utils;

import cc.funkemunky.kauri.KauriDownloader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class DownloaderUtils {
    public static void downloadAppropriateVersion() {
        File pluginLocation = MiscUtils.findPluginFile("Atlas");
        try {
            if(Bukkit.getPluginManager().getPlugin("Atlas") == null) {
                String link = "https://github.com/funkemunky/Atlas/releases/download/%ver%/Atlas.jar".replaceAll("%ver%", KauriDownloader.getInstance().getRequitedVersionOfAlias()[0]);
                InputStream in = new URL(link).openStream();
                Files.copy(in, Paths.get(pluginLocation.getPath()), StandardCopyOption.REPLACE_EXISTING);

                if(!KauriDownloader.getInstance().getServer().getPluginManager().isPluginEnabled("Atlas")) {
                    Plugin plugin = Bukkit.getPluginManager().loadPlugin(pluginLocation);
                    Bukkit.getPluginManager().enablePlugin(plugin);
                } else {
                    MiscUtils.unloadPlugin("Atlas");
                    MiscUtils.loadPlugin("Atlas");
                }
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Make sure you have the Atlas jar deleted otherwise you may receive the incorrect version.");
                Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("Atlas"));
            }
        } catch (IOException | InvalidDescriptionException | InvalidPluginException e) {
            e.printStackTrace();
        }
    }
}