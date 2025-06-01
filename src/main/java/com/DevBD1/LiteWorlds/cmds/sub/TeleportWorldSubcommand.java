package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.cmds.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeleportWorldSubcommand implements SubCommand {

    @Override
    public String getName() {
        return "tp";
    }

    @Override
    public String getDescription() {
        return "Teleport to a world.";
    }

    @Override
    public String getUsage() {
        return "/liteworld tp <worldName>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: " + getUsage());
            return;
        }

        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            player.sendMessage("World not found: " + args[0]);
            return;
        }

        player.teleport(world.getSpawnLocation());
        player.sendMessage("Teleported to world: " + world.getName());
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(Bukkit.getWorlds().stream().map(World::getName).toList());
        }
        return Collections.emptyList();
    }
}