package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.Main;
import com.DevBD1.LiteWorlds.cmds.SubCommand;
import com.DevBD1.LiteWorlds.generator.VoidWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CreateWorldSubcommand implements SubCommand {

    private final Main plugin;

    public CreateWorldSubcommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Creates a new world.";
    }

    @Override
    public String getUsage() {
        return "/liteworld create <name> <type: NORMAL|VOID|NETHER|END>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        String name = args[0];
        String type = args[1].toUpperCase();

        WorldCreator creator = new WorldCreator(name);

        switch (type) {
            case "NORMAL" -> creator.environment(org.bukkit.World.Environment.NORMAL);
            case "NETHER" -> creator.environment(org.bukkit.World.Environment.NETHER);
            case "END" -> creator.environment(org.bukkit.World.Environment.THE_END);
            case "VOID" -> {
                creator.generator(new VoidWorldGenerator());
                creator.environment(org.bukkit.World.Environment.NORMAL);
                creator.type(WorldType.FLAT);
            }
            default -> {
                sender.sendMessage("Invalid world type. Use NORMAL, VOID, NETHER or END.");
                return;
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.createWorld(creator);
            sender.sendMessage("World '" + name + "' created with type '" + type + "'.");

            // Persist to config
            FileConfiguration config = plugin.getConfig();
            List<Map<?, ?>> worlds = config.getMapList("worlds");

            boolean exists = worlds.stream().anyMatch(m -> name.equalsIgnoreCase((String) m.get("name")));
            if (!exists) {
                Map<String, Object> entry = Map.of(
                        "name", name,
                        "type", type,
                        "prevent-grief", false
                );
                worlds.add(entry);
                config.set("worlds", worlds);
                plugin.saveConfig();
            }
        });

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("NORMAL", "VOID", "NETHER", "END");
        }
        return Collections.emptyList();
    }
}
