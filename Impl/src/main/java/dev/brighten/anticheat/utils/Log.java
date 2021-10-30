package dev.brighten.anticheat.utils;


import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Log {
    public static void severe(String severe, Object... objects) {
        log(Level.SEVERE, severe, objects);
    }

    public static void info(String info, Object... objects) {
        log(Level.INFO, info, objects);
    }

    public static void warning(String warning, Object... objects) {
        log(Level.WARNING, warning, objects);
    }

    public static void log(Level level, String log, Object... objects) {
        Bukkit.getLogger().log(level, log, objects);
    }
}
