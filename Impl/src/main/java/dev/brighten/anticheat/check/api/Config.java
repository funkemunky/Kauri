package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;
import dev.brighten.api.check.DevStage;

import java.util.Arrays;
import java.util.List;

@Init
public class Config {

    @ConfigSetting(path = "punishments", name = "commands")
    static List<String> punishCommands = Arrays.asList("kick %name% [Kauri] Unfair Advantage -s");

    @ConfigSetting(path = "punishments", name = "broadcast",
            comment = "Set string to \"none\" if you want to disable broadcast.")
    static String broadcastMessage = MiscUtils.line(Color.Dark_Gray)
            + "\n&e%player% &7was removed by &6Kauri &7because of an &fUnfair Advantage&7."
            + MiscUtils.line(Color.Dark_Gray);

    @ConfigSetting(path = "punishments", name = "bungeeCommand")
    static boolean bungeePunishments = false;

    @ConfigSetting(path = "no-premium-uuid")
    public static boolean noPremiumUUID = false;

    @ConfigSetting(path = "punishments", name = "bungeeBroadcast")
    static boolean bungeeBroadcast = false;

    @ConfigSetting(path = "punishments", name = "bypassPerm")
    static boolean punishmentBypassPerm = true;

    @ConfigSetting(path = "punishments", name = "releaseStageMinimum")
    static String minimumStageBan = "Release";

    @ConfigSetting(path = "alerts", name = "bungee", comment = "Sends alerts across servers.")
    public static boolean bungeeAlerts = false;

    @ConfigSetting(path = "alerts", name = "prefixWhitelist", comment = "Whitelist users from alerts")
    public static String prefixWhitelist = "disabled";

    @ConfigSetting(path = "alerts", name = "delay", comment = "The delay between alerts sending.")
    public static long alertsDelay = 500;

    @ConfigSetting(path = "alerts", name = "toConsole", comment = "Send alert messages to console.")
    public static boolean alertsConsole = true;

    @ConfigSetting(path = "general", name = "language")
    public static String language = "english";

    @ConfigSetting(path = "alerts", name = "cancelCheats")
    public static boolean cancelCheats = true;

    @ConfigSetting(path = "alerts", name = "testMode")
    public static boolean testMode = false;

    @ConfigSetting(path = "alerts", name = "clickCommand")
    public static String alertCommand = "teleport %player%";

    @ConfigSetting(path = "alerts", name = "dev", comment = "Alert for experimental checks.")
    public static boolean alertDev = true;

    @ConfigSetting(path = "alerts", name = "bypassPerm")
    public static boolean flagBypassPerm = true;

    @ConfigSetting(path = "alerts.advanced", name = "overrideCompatibilityWarnings")
    public static boolean overrideCompat = false;

    @ConfigSetting(name = "metrics")
    public static boolean metrics = true;
}
