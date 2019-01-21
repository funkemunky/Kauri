package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class CheckSettings {
    @ConfigSetting(path = "alerts", name = "message")
    public static String alertMessage = "&8[&b&lKauri&8] &f%player% &7has failed &f%check% &c(x%vl%)";

    @ConfigSetting(path = "alerts", name = "delay")
    public static long alertsDelay = 1000L;
}
