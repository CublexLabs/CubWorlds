package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.Main;
import com.DevBD1.LiteWorlds.cmds.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class UnloadWorldSubcommand implements SubCommand {
    private final Main plugin;

    public UnloadWorldSubcommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "unload";
    }

    @Override
    public String getDescription() {
        return "Unloads a world from memory.";
    }

    @Override
    public String getUsage() {
        return "/liteworld unload <worldName>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage("World '" + worldName + "' is not currently loaded.");
            return;
        }

        if (Bukkit.unloadWorld(world, true)) {
            sender.sendMessage("World '" + worldName + "' has been unloaded.");
        } else {
            sender.sendMessage("Failed to unload world '" + worldName + "'.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}
