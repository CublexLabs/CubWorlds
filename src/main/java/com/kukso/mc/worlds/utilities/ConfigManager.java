package com.kukso.mc.worlds.utilities;

/**
 * Singleton helper to manage configuration.
 * • Call init(plugin) inside onEnable().
 * • Provides getString/getBoolean/getInt/getDouble/getList for safe reads with defaults.
 * • printStatus() logs current config settings to console.
 */

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ConfigManager {
    private static JavaPlugin plugin;
    private static FileConfiguration config;

    private ConfigManager() { }

    /**
     * Initializes the ConfigManager. Must be called from onEnable().
     */
    public static void init(JavaPlugin pl) {
        plugin = pl;
        plugin.saveDefaultConfig();

        config = plugin.getConfig();
    }

    /**
     * Logs the current configuration status to console.
     */
    public static void printStatus() {
    }

    /**
     * Validates that the config key is non-null and matches allowed characters.
     */
    private static void validateKey(String key) {
        if (key == null || !key.matches("[\\w.-]+")) {
            throw new IllegalArgumentException("Invalid config key: " + key);
        }
    }

    /**
     * Reads a string value or returns the default if missing/empty.
     */
    public static String getString(String key, String def) {
        validateKey(key);
        String v = config.getString(key);
        if (v == null || v.isEmpty()) {
            plugin.getLogger().warning("Missing/empty '" + key + "', using default: " + def);
            return def;
        }
        return v;
    }

    /**
     * Reads a boolean value or returns the default if missing.
     */
    public static boolean getBoolean(String key, boolean def) {
        validateKey(key);
        if (!config.contains(key)) {
            plugin.getLogger().warning("Missing '" + key + "', using default: " + def);
            return def;
        }
        return config.getBoolean(key);
    }

    /**
     * Reads an integer value or returns the default if missing.
     */
    public static int getInt(String key, int def) {
        validateKey(key);
        if (!config.contains(key)) {
            plugin.getLogger().warning("Missing '" + key + "', using default: " + def);
            return def;
        }
        return config.getInt(key);
    }

    /**
     * Reads a double value or returns the default if missing.
     */
    public static double getDouble(String key, double def) {
        validateKey(key);
        if (!config.contains(key)) {
            plugin.getLogger().warning("Missing '" + key + "', using default: " + def);
            return def;
        }
        return config.getDouble(key);
    }

    /**
     * Reads a List<T> or returns the default list if missing.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(String key, List<T> def) {
        validateKey(key);
        List<T> v = (List<T>) config.getList(key);
        if (v == null) {
            plugin.getLogger().warning("Missing '" + key + "', using default list.");
            return def;
        }
        return v;
    }

    /**
     * Returns the underlying FileConfiguration object.
     * @return The current configuration
     */
    public static FileConfiguration getConfig() {
        if (config == null) {
            throw new IllegalStateException("ConfigManager has not been initialized. Call init() first.");
        }
        return config;
    }
}
