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

    @ConfigSetting(path = "punishments", name = "broadcast")
    static String broadcastMessage = MiscUtils.line(Color.Dark_Gray)
            + "\n&e%name% &7was removed by &6Kauri &7because of an &fUnfair Advantage&7."
            + MiscUtils.line(Color.Dark_Gray);

    @ConfigSetting(path = "alerts", name = "bungee")
    public static boolean bungeeAlerts = false;

    @ConfigSetting(path = "alerts", name = "delay")
    public static long alertsDelay = 500;
}
