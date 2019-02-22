package cc.funkemunky.kauri.utils;

import cc.funkemunky.kauri.KauriDownloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DownloaderUtils {
    public static void downloadAppropriateVersion() {
        File pluginLocation = MiscUtils.findPluginFile("Atlas");
        String link = "https://github.com/funkemunky/Atlas/releases/download/%ver%/Atlas.jar".replaceAll("%ver%", KauriDownloader.getInstance().getRequitedVersionOfAlias()[0]);

        try {
            InputStream in = new URL(link).openStream();
            Files.copy(in, Paths.get(pluginLocation.getPath()), StandardCopyOption.REPLACE_EXISTING);

            if(!KauriDownloader.getInstance().getServer().getPluginManager().isPluginEnabled("Atlas")) {
                MiscUtils.loadPlugin("Atlas");
                pluginLocation.delete();
            } else {
                MiscUtils.unloadPlugin("Atlas");
                MiscUtils.loadPlugin("Atlas");
                pluginLocation.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
