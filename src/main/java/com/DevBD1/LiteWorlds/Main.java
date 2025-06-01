package com.DevBD1.LiteWorlds;

import com.DevBD1.LiteWorlds.cmds.LiteWorldCommand;
import com.DevBD1.LiteWorlds.cmds.LiteWorldTabCompleter;
import com.DevBD1.LiteWorlds.listener.GriefPreventionListener;
import com.DevBD1.LiteWorlds.generator.VoidWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("LiteWorld loaded.");

        // Load worlds from config
        FileConfiguration config = getConfig();
        if (config.isList("worlds")) {
            config.getMapList("worlds").forEach(worldConfig -> {
                Map<String, Object> world = (Map<String, Object>) worldConfig;
                String name = (String) world.get("name");
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
                    Bukkit.createWorld(creator);
                    getLogger().info("Loaded world: " + name);
                }, 1L);
            });
        }

        getServer().getPluginManager().registerEvents(new GriefPreventionListener(this), this);

        // Register command and tab completer
        LiteWorldCommand command = new LiteWorldCommand(this);
        getCommand("liteworld").setExecutor(command);
        getCommand("liteworld").setTabCompleter(new LiteWorldTabCompleter(command));
    }

    @Override
    public void onDisable() {

        FileConfiguration config = getConfig();
        if (config.isList("worlds")) {
            config.getMapList("worlds").forEach(raw -> {
                String name = (String) ((Map<?, ?>) raw).get("name");
                if (name != null && Bukkit.getWorld(name) != null) {
                    Bukkit.unloadWorld(name, true); // true = save before unloading
                    getLogger().info("Unloaded world: " + name);
                }
            });
        }

        getLogger().info("LiteWorld disabled.");

    }
}