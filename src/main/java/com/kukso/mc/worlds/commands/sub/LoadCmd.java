package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import com.kukso.mc.worlds.generator.VoidWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LoadCmd implements CmdInterface {
    String CMD_NAME = "load";
    private final Main plugin;

    public LoadCmd(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public List<String> getAliases() {
        return CmdConfig.getAliases(CMD_NAME);
    }

    @Override
    public List<String> getPermissions() {
        return CmdConfig.getPermissions(CMD_NAME);
    }

    @Override
    public String getDescription() {
        return "Loads a world defined in config.";
    }

    @Override
    public String getUsage() {
        return "/kuksoworlds load <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            send(sender, "general.usage", Map.of("usage", getUsage()));
            return false;
        }

        String worldName = args[0];
        FileConfiguration config = plugin.getConfig();
        List<Map<String, Object>> worlds = (List<Map<String, Object>>) (List<?>) config.getMapList("worlds");

        for (Map<?, ?> entry : worlds) {
            Map<String, Object> typedEntry = (Map<String, Object>) entry;
            String name = (String) typedEntry.get("name");
            if (name.equalsIgnoreCase(worldName)) {
                if (Bukkit.getWorld(name) != null) {
                    send(sender, "commands.load.already-loaded", Map.of("name", name));
                    return false;
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
                send(sender, "commands.load.success", Map.of("name", worldName));
                return false;
            }
        }

        send(sender, "commands.load.not-found", Map.of("name", worldName));
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
