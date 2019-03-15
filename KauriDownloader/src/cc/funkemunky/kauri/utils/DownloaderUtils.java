package cc.funkemunky.kauri.utils;

import cc.funkemunky.kauri.KauriDownloader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class DownloaderUtils {
    public static void downloadAppropriateVersion() {
        File pluginLocation = MiscUtils.findPluginFile("Atlas");
        try {
            if(Bukkit.getPluginManager().getPlugin("Atlas") == null) {
                String link = "https://github.com/funkemunky/Atlas/releases/download/%ver%/Atlas.jar".replaceAll("%ver%", readFromUpdaterPastebin());
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

    private static String readFromUpdaterPastebin() {
        try {
            URL url = new URL("https://pastebin.com/raw/1G0kFzkx");
            URLConnection connection = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = reader.readLine();

            if(line != null) return line;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "1.1.3.3";
    }
}