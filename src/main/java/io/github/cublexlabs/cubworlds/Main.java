package io.github.cublexlabs.cubworlds;

import io.github.cublexlabs.cubworlds.cmds.CmdRegistrar;
import io.github.cublexlabs.cubworlds.listener.GriefPreventionListener;
import io.github.cublexlabs.cubworlds.listener.WorldAccessListener;
import io.github.cublexlabs.cubworlds.utilities.ConfigManager;
import io.github.cublexlabs.cubworlds.utilities.PlaceholderAPIExtension;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class Main extends JavaPlugin {

    private static Main instance;
    private WorldLoader worldLoader;

//    public static Main getInstance() {
//        return instance;
//    }

    public WorldLoader getWorldLoader() {
        return worldLoader;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getLogger().info("[CubWorlds] Plugin starting...");

        try {
            // Tek bir WorldLoader örneği
            this.worldLoader = new WorldLoader(this);

            // ConfigManager init
            ConfigManager.init(this);

            // Dünyaları config’ten yükle
            for (Map<?, ?> raw : getConfig().getMapList("worlds")) {
                String name = (String) raw.get("name");
                if (name == null || name.isBlank()) {
                    getLogger().warning("Skipping world entry without a valid name.");
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> worldMap = (Map<String, Object>) raw;
                worldLoader.loadWorld(worldMap);
            }

            // Event & Command kayıtları
            getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
            getServer().getPluginManager().registerEvents(new WorldAccessListener(this), this);
            CmdRegistrar.register(this);

            getLogger().info("[CubWorlds] Plugin loaded successfully.");
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
            getLogger().info("CubWorlds disabled.");
        } catch (Exception e) {
            getLogger().severe("Error during plugin shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
