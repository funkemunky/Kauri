package dev.brighten.anticheat.check.impl;

import cc.funkemunky.api.utils.ConfigSetting;
import cc.funkemunky.api.utils.Init;
import cc.funkemunky.api.utils.Priority;

import java.util.Arrays;
import java.util.List;

@Init(priority = Priority.HIGHEST)
public class ConfigAra {
    @ConfigSetting(path = "customization", name = "command-names")
    public static List<String> commandAlias = Arrays.asList("kauri", "anticheat");
}
