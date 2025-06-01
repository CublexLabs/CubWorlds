package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.cmds.SubCommand;
import com.DevBD1.LiteWorlds.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TeleportWorldSubcommand implements SubCommand {
    private final Main plugin;

    public TeleportWorldSubcommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "teleport";
    }

    @Override
    public String getDescription() {
        return "Teleports to a world spawn.";
    }

    @Override
    public String getUsage() {
        return "/liteworld teleport <worldName>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can teleport.");
            return;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage("World is not loaded. Load it first using /liteworld load " + worldName);
            return;
        }

        player.teleport(world.getSpawnLocation());
        sender.sendMessage("Teleported to world: " + worldName);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}