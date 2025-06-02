package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.Main;
import com.DevBD1.LiteWorlds.cmds.SubCommand;
import com.DevBD1.LiteWorlds.generator.VoidWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LoadWorldSubcommand implements SubCommand {
    private final Main plugin;

    public LoadWorldSubcommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getDescription() {
        return "Loads a world defined in config.";
    }

    @Override
    public String getUsage() {
        return "/liteworld load <worldName>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        String worldName = args[0];
        FileConfiguration config = plugin.getConfig();
        List<Map<String, Object>> worlds = (List<Map<String, Object>>) (List<?>) config.getMapList("worlds");

        for (Map<?, ?> entry : worlds) {
            Map<String, Object> typedEntry = (Map<String, Object>) entry;
            String name = (String) typedEntry.get("name");
            if (name.equalsIgnoreCase(worldName)) {
                if (Bukkit.getWorld(name) != null) {
                    sender.sendMessage("World '" + name + "' is already loaded.");
                    return;
                }
                String type = ((String) typedEntry.getOrDefault("type", "NORMAL")).toUpperCase();

                WorldCreator creator = new WorldCreator(name);
                switch (type) {
                    case "NETHER" -> creator.environment(org.bukkit.World.Environment.NETHER);
                    case "END" -> creator.environment(org.bukkit.World.Environment.THE_END);
                    case "VOID" -> {
                        creator.generator(new VoidWorldGenerator());
                        creator.environment(org.bukkit.World.Environment.NORMAL);
                        creator.type(WorldType.FLAT);
                    }
                    default -> creator.environment(org.bukkit.World.Environment.NORMAL);
                }

                Bukkit.createWorld(creator);
                sender.sendMessage("World '" + worldName + "' has been loaded.");
                return;
            }
        }

        sender.sendMessage("World '" + worldName + "' not found in config.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}