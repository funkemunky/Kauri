package dev.brighten.anticheat.logs.data.config;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class MongoConfig {
    @ConfigSetting(path = "database.mongo", name = "enabled")
    public static boolean enabled = false;

    @ConfigSetting(path = "database.mongo", name = "username", hide = true)
    public static String username = "root";

    @ConfigSetting(path = "database.mongo", name = "password", hide = true)
    public static String password = "password";

    @ConfigSetting(path = "database.mongo", name = "requiresLoginDetails", hide = true)
    public static boolean loginDetails = false;

    @ConfigSetting(path = "database.mongo", name = "database", hide = true)
    public static String database = "Kauri";

    @ConfigSetting(path = "database.mongo", name = "authDatabase", hide = true)
    public static String authDatabase = "admin";

    @ConfigSetting(path = "database.mongo", name = "ip", hide = true)
    public static String ip = "127.0.0.1";

    @ConfigSetting(path = "database.mongo", name = "port", hide = true)
    public static int port = 27017;

    @ConfigSetting(path = "database.mongo", name = "connectionURL", hide = true)
    public static String connectionURL = "This will override your connection details";

    @ConfigSetting(path = "database.mongo", name = "batchInsertMax")
    public static int batchInsertMax = 250;
}
