package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.MiscUtils;

import java.util.Arrays;
import java.util.List;

@Init
public class Config {

    @ConfigSetting(path = "punishments", name = "commands")
    static List<String> punishCommands = Arrays.asList("kick %name% [Kauri] Unfair Advantage -s");

    @ConfigSetting(path = "punishments", name = "broadcast",
            comment = "Set string to \"none\" if you want to disable broadcast.")
    static String broadcastMessage = MiscUtils.line(Color.Dark_Gray)
            + "\n&e%name% &7was removed by &6Kauri &7because of an &fUnfair Advantage&7."
            + MiscUtils.line(Color.Dark_Gray);

    @ConfigSetting(path = "punishments", name = "bungeeCommand")
    static boolean bungeePunishments = false;

    @ConfigSetting(path = "punishments", name = "bungeeBroadcast")
    static boolean bungeeBroadcast = false;

    @ConfigSetting(path = "alerts", name = "bungee", comment = "Sends alerts across servers.")
    public static boolean bungeeAlerts = false;

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

    @ConfigSetting(name = "bypassPerm")
    public static boolean bypassPermission = true;

    @ConfigSetting(path = "alerts.advanced", name = "overrideCompatibilityWarnings")
    public static boolean overrideCompat = false;

    @ConfigSetting(name = "metrics")
    public static boolean metrics = true;
}
