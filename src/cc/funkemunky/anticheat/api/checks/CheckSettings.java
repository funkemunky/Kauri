package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;

@Init(priority = Priority.HIGHEST)
public class CheckSettings {
    @ConfigSetting(name = "executableCommand")
    static String executableCommand = "kick %player% [Kauri] You have been kicked for failing %check%";

    @ConfigSetting(path = "alerts", name = "alertMessage")
    static String alertMessage = "&8[&b&lKauri&8] &f%player% &7has failed &f%check% &c(x%vl%)";

    @ConfigSetting(path = "alerts", name = "alertsDelay")
    static long alertsDelay = 1000;

    @ConfigSetting(path = "alerts", name = "testMode")
    public static boolean testMode = false;

    @ConfigSetting(path = "alerts", name = "printToConsole")
    static boolean printToConsole = false;

    @ConfigSetting(path = "alerts", name = "enableOnJoin")
    public static boolean enableOnJoin = true;
}
