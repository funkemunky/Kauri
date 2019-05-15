package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;

import java.util.Collections;
import java.util.List;

@Init(priority = Priority.HIGHEST)
public class CheckSettings {
    @ConfigSetting(name = "executableCommand")
    static List<String> executableCommand = Collections.singletonList("ban %player% [Kauri] Unfair Advantage");

    @ConfigSetting(path = "alerts", name = "prefix")
    static String alertPrefix = "&8[&6&lKauri&8]";

    @ConfigSetting(path = "alerts", name = "devPrefix")
    static String devAlertPrefix = "&8[&c&lDEV&8]";

    @ConfigSetting(path = "alerts", name = "alertMessage")
    static String alertMessage = "%prefix% &f%player% &7failed &f%check% &8(&7vl=&f%vl%&7, &7chance=&f%chance%&8)";

    @ConfigSetting(path = "executable.broadcast", name = "enabled")
    static boolean broadcastEnabled = false;

    @ConfigSetting(path = "executable.broadcast", name = "message")
    static String broadcastMessage = "&8--------------------------------\n&6&lKauri &7has removed &e%player% from the server for cheating.\n&8--------------------------------";

    @ConfigSetting(path = "alerts", name = "alertsDelay")
    public static long alertsDelay = 1000;

    @ConfigSetting(path = "alerts", name = "testMode")
    public static boolean testMode = false;

    @ConfigSetting(path = "alerts", name = "printToConsole")
    static boolean printToConsole = false;

    @ConfigSetting(path = "alerts", name = "enableOnJoin")
    public static boolean enableOnJoin = true;

    @ConfigSetting(path = "bypass", name = "enabled")
    public static boolean bypassEnabled = true;

    @ConfigSetting(path = "bypass", name = "permission")
    public static String bypassPermission = "kauri.bypass";

    @ConfigSetting(path = "alerts", name = "showExperimentalAlerts")
    public static boolean showExperimentalAlerts = false;

    /*@ConfigSetting(path = "alerts", name = "defaultMode")
    public static AlertTier defaultAlertMode = AlertTier.HIGH;*/

    @ConfigSetting(path = "alerts", name = "tpsThreshold")
    public static double tpsThreshold = 14.0;

    @ConfigSetting(path = "antivpn.license", name = "code")
    public static String license = "";

    @ConfigSetting(path = "antivpn.license", name = "override")
    public static boolean override = false;
}
