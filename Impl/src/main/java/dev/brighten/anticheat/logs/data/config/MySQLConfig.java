package dev.brighten.anticheat.logs.data.config;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class MySQLConfig {
    @ConfigSetting(path = "database.mysql", name = "enabled")
    public static boolean enabled = false;

    @ConfigSetting(path = "database.mysql", name = "username", hide = true)
    public static String username = "root";

    @ConfigSetting(path = "database.mysql", name = "database", hide = true)
    public static String database = "Kauri";

    @ConfigSetting(path = "database.mysql", name = "password", hide = true)
    public static String password = "password";

    @ConfigSetting(path = "database.mysql", name = "ip", hide = true)
    public static String ip = "127.0.0.1";

    @ConfigSetting(path = "database.mysql", name = "port", hide = true)
    public static int port = 3306;

    @ConfigSetting(path = "database.mysql", name = "debugMessages",
            comment = "This will insert debug messages into console about MySQL insertion, among other things.\n" +
            "Recommended to keep disabled unless otherwise instructed.")
    public static boolean debugMessages = false;

    @ConfigSetting(path = "database.mysql", name = "insertionRate")
    public static int rateInSeconds = 2;
}
