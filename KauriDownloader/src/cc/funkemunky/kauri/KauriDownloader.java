package cc.funkemunky.kauri;

import cc.funkemunky.kauri.utils.DownloaderUtils;
import cc.funkemunky.kauri.utils.ReflectionsUtil;
import lombok.Getter;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

@Getter
public class KauriDownloader extends JavaPlugin {

    @Getter
    private static KauriDownloader instance;
    private String[] requitedVersionOfAlias = new String[] {"1.1.3"};

    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        DownloaderUtils.downloadAppropriateVersion();

        try {
            URL url = new URL("https://funkemunky.cc/download?name=Kauri_Lite&license=" + getConfig().getString("license") + "&version=" + getDescription().getVersion());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                is = url.openStream ();
                byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
                int n;

                while ( (n = is.read(byteChunk)) > 0 ) {
                    baos.write(byteChunk, 0, n);
                }


                File kauri;
                if(ReflectionsUtil.version.contains("7")) {
                    kauri = new File("plugins", net.minecraft.util.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(30) + ".jar");
                    net.minecraft.util.org.apache.commons.io.FileUtils.writeByteArrayToFile(kauri, byteChunk);
                } else {
                    kauri = new File("plugins", org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(30) + ".jar");
                    org.apache.commons.io.FileUtils.writeByteArrayToFile(kauri, byteChunk);
                }

                getLogger().log(Level.INFO, "Downloaded! Loading plugin...");
                Plugin plugin = getServer().getPluginManager().loadPlugin(kauri);
                is.close();
                getServer().getPluginManager().enablePlugin(plugin);
                if (kauri.delete()) getLogger().log(Level.INFO, "Loaded successfully!");
            }
            catch (IOException e) {
                System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
                e.printStackTrace ();
                // Perform any other exception handling that's appropriate.
            } catch (InvalidDescriptionException | InvalidPluginException e) {
                e.printStackTrace();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
