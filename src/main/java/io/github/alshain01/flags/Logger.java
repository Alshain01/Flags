package io.github.alshain01.flags;

import org.bukkit.Bukkit;

public class Logger {
    private static java.util.logging.Logger logger = Bukkit.getServer().getPluginManager().getPlugin("Flags").getLogger();
    private static boolean debugEnabled = Bukkit.getServer().getPluginManager().getPlugin("Flags").getConfig().getBoolean("Flags.Debug");

    public enum DebugCategory {
        SQL, Area, Flag
    }

    public static void debug(String message) {
        if(debugEnabled) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void error(String message) {
        logger.severe(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void info(String message) {
        logger.info(message);
    }
}
