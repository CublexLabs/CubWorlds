package com.DevBD1.LiteWorlds.cmds.sub;

import com.DevBD1.LiteWorlds.Main;
import com.DevBD1.LiteWorlds.cmds.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DeleteWorldSubcommand implements SubCommand {
    private final Main plugin;

    public DeleteWorldSubcommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Unloads and deletes a world from disk.";
    }

    @Override
    public String getUsage() {
        return "/liteworld delete <worldName>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        FileConfiguration config = plugin.getConfig();
        String fallbackName = config.getString("fallback-world", "world");
        World fallback = Bukkit.getWorld(fallbackName);

        if (world != null && fallback != null) {
            int relocated = 0;
            for (Player player : world.getPlayers()) {
                player.teleport(fallback.getSpawnLocation());
                relocated++;
            }
            Bukkit.unloadWorld(world, true);
            sender.sendMessage("World '" + worldName + "' unloaded. " + relocated + " player(s) relocated.");
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            sender.sendMessage("World folder '" + worldName + "' does not exist.");
            return;
        }

        try {
            //deleteRecursively(worldFolder.toPath());
            File deletedFolder = new File(Bukkit.getWorldContainer(), "recycle_bin/" + worldName);
            deletedFolder.getParentFile().mkdirs();
            Files.move(worldFolder.toPath(), deletedFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);

            sender.sendMessage("World folder '" + worldName + "' has been deleted successfully.");
        } catch (IOException e) {
            sender.sendMessage("Failed to delete world folder '" + worldName + "': " + e.getMessage());
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete " + p, e);
                    }
                });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}
