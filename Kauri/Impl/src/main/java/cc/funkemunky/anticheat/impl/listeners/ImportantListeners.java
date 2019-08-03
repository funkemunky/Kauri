package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;

@Init(priority = Priority.HIGHEST)
public class ImportantListeners {

    @ConfigSetting(path = "kauri.customize.plugins", name = "message")
    static String message = "&fPlugins (%length%): &f{plugins}";

    @ConfigSetting(path = "kauri.customize.plugins", name = "pluginFormat")
    static String pluginFormat = "&a{plugin}&f, ";

    @ConfigSetting(path = "kauri.customize.plugins", name = "enabled")
    static boolean pluginsEnabled = true;

    @ConfigSetting(path = "kauri.customize.hideKauri", name = "enabled")
    static boolean hideKauri = true;

    @ConfigSetting(path = "kauri.customize.hideAtlas", name = "enabled")
    static boolean hideAtlas = true;

    @ConfigSetting(path = "kauri.customize.hideKauri", name = "replace.enabled")
    static boolean replaceKauri = true;

    @ConfigSetting(path = "kauri.customize.hideKauri", name = "replace.string")
    static String replaceKauriString = "Anticheat";

    @ConfigSetting(path = "kauri.customize", name = "enabled")
    static boolean enabled = false;

    @ConfigSetting(path = "kauri.customize", name = "license")
    public static String license = "Insert your Kauri Lifetime license or Customizer License here.";

    @ConfigSetting(path = "kauri.customize", name = "customizeSubscription")
    public static boolean customSub = false;

    @ConfigSetting(path = "kauri.customize.command.main", name = "name")
    public static String mainCommand = "kauri";

    @ConfigSetting(path = "kauri.customize.command.main", name = "display")
    public static String mainDisplay = "kauri";
}
