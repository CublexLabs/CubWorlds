package io.github.devbd1.CubWorlds;

import io.github.devbd1.CubWorlds.cmds.CmdRegistrar;
import io.github.devbd1.CubWorlds.listener.GriefPreventionListener;
import io.github.devbd1.CubWorlds.generator.VoidWorldGenerator;
import io.github.devbd1.CubWorlds.listener.WorldAccessListener;
import io.github.devbd1.CubWorlds.utilities.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class Main extends JavaPlugin {

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }
    @Override
    public void onEnable() {
        try {
            instance = this;
            saveDefaultConfig();
            getLogger().info("CubWorld loaded.");

            ConfigManager.init(this);

            // Load worlds from config
            FileConfiguration config = getConfig();
            if (config.isList("worlds")) {
                for (Object worldConfigObj : config.getMapList("worlds")) {
                    try {
                        Map<String, Object> world = (Map<String, Object>) worldConfigObj;
                        String name = (String) world.get("name");
                        if (name == null) {
                            getLogger().warning("Skipping world with missing name property");
                            continue;
                        }
                        
                        String type = ((String) world.getOrDefault("type", "NORMAL")).toUpperCase();

                        WorldCreator creator = new WorldCreator(name);
                        switch (type) {
                            case "NETHER" -> creator.environment(org.bukkit.World.Environment.NETHER);
                            case "END" -> creator.environment(org.bukkit.World.Environment.THE_END);
                            case "VOID" -> {
                                creator.generator(new VoidWorldGenerator());
                                creator.environment(org.bukkit.World.Environment.NORMAL);
                            }
                            default -> creator.environment(org.bukkit.World.Environment.NORMAL);
                        }

                        Bukkit.getScheduler().runTaskLater(this, () -> {
                            try {
                                Bukkit.createWorld(creator);
                                getLogger().info("Loaded world: " + name);
                            } catch (Exception e) {
                                getLogger().severe("Failed to load world " + name + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }, 1L);
                    } catch (ClassCastException e) {
                        getLogger().severe("Invalid world configuration entry: " + e.getMessage());
                    } catch (Exception e) {
                        getLogger().severe("Error processing world configuration: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            try {
                getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);
            } catch (Exception e) {
                getLogger().severe("Failed to register GriefPreventionListener: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                // Register commands
                CmdRegistrar.register(this);
            } catch (Exception e) {
                getLogger().severe("Failed to register commands: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                getServer().getPluginManager().registerEvents(new WorldAccessListener(this), this);
            } catch (Exception e) {
                getLogger().severe("Failed to register WorldAccessListener: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            getLogger().severe("Critical error during plugin initialization: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            FileConfiguration config = getConfig();
            if (config.isList("worlds")) {
                for (Object rawWorld : config.getMapList("worlds")) {
                    try {
                        if (!(rawWorld instanceof Map)) {
                            getLogger().warning("Invalid world configuration entry during unload");
                            continue;
                        }
                        
                        Map<?, ?> worldMap = (Map<?, ?>) rawWorld;
                        String name = (String) worldMap.get("name");
                        
                        if (name == null) {
                            getLogger().warning("World entry missing name property during unload");
                            continue;
                        }
                        
                        if (Bukkit.getWorld(name) != null) {
                            try {
                                boolean success = Bukkit.unloadWorld(name, true); // true = save before unloading
                                if (success) {
                                    getLogger().info("Unloaded world: " + name);
                                } else {
                                    getLogger().warning("Failed to unload world: " + name);
                                }
                            } catch (Exception e) {
                                getLogger().severe("Error unloading world " + name + ": " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        getLogger().severe("Error processing world during shutdown: " + e.getMessage());
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