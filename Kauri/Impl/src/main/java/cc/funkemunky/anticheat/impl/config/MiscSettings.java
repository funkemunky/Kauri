package cc.funkemunky.anticheat.impl.config;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class MiscSettings {

    @ConfigSetting(path = "values", name = "serverPos")
    public static long serverPos = 100;
}
