package com.kukso.mc.worlds;

import com.kukso.mc.worlds.commands.CmdRegistrar;
import com.kukso.mc.worlds.hooks.PlaceholderAPIExtension;
import com.kukso.mc.worlds.utilities.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class Main extends JavaPlugin {

    private static Main instance;
    private WorldLoader worldLoader;
    private boolean kuksoLibLoaded;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getLogger().info("[KuksoWorlds] Plugin is starting...");

        Plugin kuksoLib = getServer().getPluginManager().getPlugin("KuksoLib");
        this.kuksoLibLoaded = (kuksoLib != null && kuksoLib.isEnabled());

        try {
            this.worldLoader = new WorldLoader(this);
            ConfigManager.init(this);
            
            // Initialize Localization
            if (kuksoLibLoaded && com.kukso.mc.lib.services.KuksoAPIProvider.isAvailable()) {
                 saveResource("lang/en.yml", false);
                 saveResource("lang/tr.yml", false);
                 com.kukso.mc.lib.services.KuksoAPIProvider.get().getLocalizationManager(this).load();
            }

            for (Map<?, ?> raw : getConfig().getMapList("worlds")) {
                String name = (String) raw.get("name");
                if (name == null || name.isBlank()) {
                    getLogger().warning("[KuksoWorlds] Skipping a world entry without a valid name.");
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> worldMap = (Map<String, Object>) raw;
                worldLoader.loadWorld(worldMap);
            }

            /// Event registering
            EventRegistrar.register(getServer(), this);
            /// Command registering
            CmdRegistrar.register(this);

            getLogger().info("[KuksoWorlds] Plugin loaded successfully.");
        } catch (Exception e) {
            getLogger().severe("Critical error during plugin initialization: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new PlaceholderAPIExtension(this).register(); //
        }
    }

    @Override
    public void onDisable() {
        try {
            FileConfiguration config = getConfig();
            if (config.isList("worlds")) {
                for (Object rawWorld : config.getMapList("worlds")) {
                    if (!(rawWorld instanceof Map<?, ?> worldMap)) {
                        getLogger().warning("Invalid world configuration entry during unload");
                        continue;
                    }

                    String name = (String) worldMap.get("name");
                    if (name == null || name.isBlank()) {
                        getLogger().warning("World entry missing name property during unload");
                        continue;
                    }

                    if (Bukkit.getWorld(name) != null) {
                        boolean success = Bukkit.unloadWorld(name, true); // save before unloading
                        if (success) {
                            getLogger().info("Unloaded world: " + name);
                        } else {
                            getLogger().warning("Failed to unload world: " + name);
                        }
                    }
                }
            }
            getLogger().info("KuksoWorlds disabled.");
        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public WorldLoader getWorldLoader() {
        return worldLoader;
    }

    public boolean isKuksoLibLoaded() {
        return kuksoLibLoaded;
    }
}
