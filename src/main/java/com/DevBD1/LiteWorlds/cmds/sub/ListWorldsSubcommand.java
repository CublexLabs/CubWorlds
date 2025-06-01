package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.Main;
import com.DevBD1.LiteWorlds.cmds.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ListWorldsSubcommand implements SubCommand {
    private final Main plugin;

    public ListWorldsSubcommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists all registered worlds and their status.";
    }

    @Override
    public String getUsage() {
        return "/liteworld list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> worlds = config.getMapList("worlds");

        if (worlds.isEmpty()) {
            sender.sendMessage("No worlds registered in config.");
            return;
        }

        sender.sendMessage("Registered worlds:");
        for (Map<?, ?> world : worlds) {
            String name = (String) world.get("name");
            boolean isLoaded = Bukkit.getWorld(name) != null;
            sender.sendMessage("- " + name + " (" + (isLoaded ? "LOADED" : "UNLOADED") + ")");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}