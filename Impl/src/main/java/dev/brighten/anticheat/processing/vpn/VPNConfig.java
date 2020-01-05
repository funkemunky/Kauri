package dev.brighten.anticheat.processing.vpn;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

@Init
public class VPNConfig {

    @ConfigSetting(path = "antivpn", name = "cacheEnabled")
    static boolean cacheResults = true;
    @ConfigSetting(path = "antivpn", name = "expire")
    static long daysToExpireCache = 7;
    @ConfigSetting(path = "antivpn", name = "enabled")
    static boolean vpnEnabled = true;
    @ConfigSetting(path = "antivpn", name = "kick")
    static boolean kick = true;
    @ConfigSetting(path = "antivpn.alert", name = "enabled")
    static boolean alert = true;
    @ConfigSetting(path = "antivpn.alert", name = "msgCommand")
    static String alertCommand = "teleport %player%";
    @ConfigSetting(path = "antivpn.alert", name = "hideIP")
    static boolean hideIP = true;
    @ConfigSetting(path = "antivpn", name = "license")
    static String license = "none";
}
