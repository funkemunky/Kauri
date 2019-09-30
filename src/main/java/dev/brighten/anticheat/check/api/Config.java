package dev.brighten.anticheat.check.api;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;

import java.util.Arrays;
import java.util.List;

@Init
public class Config {

    @ConfigSetting(path = "punishments", name = "commands")
    static List<String> punishCommands = Arrays.asList("kick %name% [Kauri] you suck");

    @ConfigSetting(path = "punishments", name = "broadcast")
    static String broadcastMessage = "&7Bruh kachigga &e%name% got banned for cheating by your boi &6&lKauri 2.0 &7!!";
}
