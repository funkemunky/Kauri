package cc.funkemunky.kauri.utils;

import cc.funkemunky.kauri.KauriDownloader;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class MiscUtils {

    public static String unloadPlugin(String pl) {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        SimplePluginManager spm = (SimplePluginManager)pm;
        SimpleCommandMap cmdMap = null;
        List plugins = null;
        Map names = null;
        Map commands = null;
        Map listeners = null;
        boolean reloadlisteners = true;
        if(spm != null) {
            try {
                Field tp = spm.getClass().getDeclaredField("plugins");
                tp.setAccessible(true);
                plugins = (List)tp.get(spm);
                Field arr$ = spm.getClass().getDeclaredField("lookupNames");
                arr$.setAccessible(true);
                names = (Map)arr$.get(spm);

                Field len$;
                try {
                    len$ = spm.getClass().getDeclaredField("listeners");
                    len$.setAccessible(true);
                    listeners = (Map)len$.get(spm);
                } catch (Exception var19) {
                    reloadlisteners = false;
                }

                len$ = spm.getClass().getDeclaredField("commandMap");
                len$.setAccessible(true);
                cmdMap = (SimpleCommandMap)len$.get(spm);
                Field i$ = cmdMap.getClass().getDeclaredField("knownCommands");
                i$.setAccessible(true);
                commands = (Map)i$.get(cmdMap);
            } catch (IllegalAccessException | NoSuchFieldException var20) {
                return "Failed to unload plugin!";
            }
        }

        String var21 = "";
        Plugin[] var22 = Bukkit.getServer().getPluginManager().getPlugins();
        int var23 = var22.length;

        for(int var24 = 0; var24 < var23; ++var24) {
            Plugin p = var22[var24];
            if(p.getDescription().getName().equalsIgnoreCase(pl)) {
                pm.disablePlugin(p);
                var21 = var21 + p.getName() + " ";
                if(plugins != null && plugins.contains(p)) {
                    plugins.remove(p);
                }

                if(names != null && names.containsKey(pl)) {
                    names.remove(pl);
                }

                Iterator it;
                if(listeners != null && reloadlisteners) {
                    it = listeners.values().iterator();

                    while(it.hasNext()) {
                        SortedSet entry = (SortedSet)it.next();
                        Iterator c = entry.iterator();

                        while(c.hasNext()) {
                            RegisteredListener value = (RegisteredListener)c.next();
                            if(value.getPlugin() == p) {
                                c.remove();
                            }
                        }
                    }
                }

                if(cmdMap != null) {
                    it = commands.entrySet().iterator();

                    while(it.hasNext()) {
                        Map.Entry var25 = (Map.Entry) it.next();
                        if(var25.getValue() instanceof PluginCommand) {
                            PluginCommand var26 = (PluginCommand)var25.getValue();
                            if(var26.getPlugin() == p) {
                                var26.unregister(cmdMap);
                                it.remove();
                            }
                        }
                    }
                }
            }
        }

        return var21 + "has been unloaded and disabled!";
    }

    public static void loadPlugin(final String pl) {
        Plugin targetPlugin = null;
        String msg = "";
        final File pluginDir = new File("plugins");
        if (!pluginDir.isDirectory()) {
            return;
        }
        File pluginFile = new File(pluginDir, pl + ".jar");
        if (!pluginFile.isFile()) {
            for (final File f : pluginDir.listFiles()) {
                try {
                    if (f.getName().endsWith(".jar")) {
                        final PluginDescriptionFile pdf = KauriDownloader.getInstance().getPluginLoader().getPluginDescription(f);
                        if (pdf.getName().equalsIgnoreCase(pl)) {
                            pluginFile = f;
                            msg = "(via search) ";
                            break;
                        }
                    }
                }
                catch (InvalidDescriptionException e2) {
                    return;
                }
            }
        }
        try {
            KauriDownloader.getInstance().getServer().getPluginManager().loadPlugin(pluginFile);
            targetPlugin = getPlugin(pl);
            KauriDownloader.getInstance().getServer().getPluginManager().enablePlugin(targetPlugin);
        }
        catch (UnknownDependencyException | InvalidPluginException | InvalidDescriptionException e3) {
            e3.printStackTrace();
        }
    }

    public static File findPluginFile(String name) {
        File pluginDir = getPluginDirectory();
        File pluginFile = new File(pluginDir, name + ".jar");
        if (!pluginFile.isFile()) {
            for (final File f : Objects.requireNonNull(pluginDir.listFiles())) {
                try {
                    if (f.getName().endsWith(".jar")) {
                        final PluginDescriptionFile pdf = KauriDownloader.getInstance().getPluginLoader().getPluginDescription(f);
                        if (pdf.getName().equalsIgnoreCase(name)) {
                            pluginFile = f;
                            break;
                        }
                    }
                }
                catch (InvalidDescriptionException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return pluginFile;
    }

    public static File getPluginDirectory() {
        File pluginDir = new File("plugins");
        if (!pluginDir.isDirectory()) {
            pluginDir = ReflectionsUtil.getPluginFolder();
        }
        return pluginDir;
    }

    private static Plugin getPlugin(final String p) {
        for (final Plugin pl : KauriDownloader.getInstance().getServer().getPluginManager().getPlugins()) {
            if (pl.getDescription().getName().equalsIgnoreCase(p)) {
                return pl;
            }
        }
        return null;
    }
}
