package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeleteCmd implements CmdInterface {
    String CMD_NAME = "delete";
    private final Main plugin;

    public DeleteCmd(Main plugin) {
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
        return "Unloads and deletes a world from disk.";
    }

    @Override
    public String getUsage() {
        return "/kuksoworlds delete <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            if (args.length < 1) {
                send(sender, "general.usage", Map.of("usage", getUsage()));
                return false;
            }

            String worldName = args[0];
            World world = Bukkit.getWorld(worldName);
            FileConfiguration config = plugin.getConfig();
            String fallbackName = config.getString("fallback-world", "world");
            World fallback = Bukkit.getWorld(fallbackName);

            // Don't allow deleting the fallback world
            if (worldName.equals(fallbackName)) {
                send(sender, "commands.delete.cannot-delete-fallback", Map.of("name", fallbackName));
                return false;
            }

            // Teleport players if world is loaded
            if (world != null) {
                if (fallback == null) {
                    send(sender, "commands.delete.fallback-not-loaded", Map.of("name", fallbackName));
                    return false;
                }

                int relocated = 0;
                for (Player player : world.getPlayers()) {
                    player.teleport(fallback.getSpawnLocation());
                    relocated++;
                }

                if (Bukkit.unloadWorld(world, true)) {
                    send(sender, "commands.delete.unloaded", Map.of("name", worldName, "count", String.valueOf(relocated)));
                } else {
                    send(sender, "commands.delete.unload-failed", Map.of("name", worldName));
                    return false;
                }
            } else {
                send(sender, "commands.delete.not-loaded", Map.of("name", worldName));
            }

            // Check if world folder exists
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (!worldFolder.exists()) {
                send(sender, "commands.delete.folder-missing", Map.of("name", worldName));
                
                // Even if folder doesn't exist, we should still remove from config
                boolean removed = removeFromConfig(worldName);
                if (removed) {
                    send(sender, "commands.delete.config-removed", Map.of("name", worldName));
                    return true;
                } else {
                    send(sender, "commands.delete.config-missing", Map.of("name", worldName));
                    return false;
                }
            }

            // Move world folder to recycle bin with timestamp
            try {
                // Create timestamped folder name to avoid conflicts
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String timestamp = dateFormat.format(new Date());
                
                File recycleBinDir = new File(Bukkit.getWorldContainer(), "recycle_bin");
                recycleBinDir.mkdirs();
                
                // Format: recycle_bin/worldname_YYYY-MM-DD_HH-MM-SS
                File deletedFolder = new File(recycleBinDir, worldName + "_" + timestamp);
                
                if (deletedFolder.exists()) {
                    // Extremely unlikely, but just in case
                    send(sender, "commands.delete.recycle-conflict");
                    deletedFolder = new File(recycleBinDir, worldName + "_" + timestamp + "_" + System.currentTimeMillis());
                }
                
                // Move the world folder to the recycle bin
                Files.move(worldFolder.toPath(), deletedFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                send(sender, "commands.delete.moved-recycle", Map.of("name", worldName));
                
                // Remove from config
                boolean removed = removeFromConfig(worldName);
                if (removed) {
                    send(sender, "commands.delete.removed-complete", Map.of("name", worldName));
                    
                    // Check recycle bin size and warn if it's getting large
                    checkRecycleBinSize(sender);
                    
                    return true;
                } else {
                    send(sender, "commands.delete.config-missing-note");
                    return false;
                }
            } catch (IOException e) {
                send(sender, "commands.delete.failed", Map.of("name", worldName, "error", e.getMessage()));
                plugin.getLogger().severe("Error deleting world " + worldName + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            send(sender, "general.unexpected-error", Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
            plugin.getLogger().severe("Error in delete command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks the size of the recycle bin and warns if it's getting large
     */
    private void checkRecycleBinSize(CommandSender sender) {
        File recycleBin = new File(Bukkit.getWorldContainer(), "recycle_bin");
        if (recycleBin.exists() && recycleBin.isDirectory()) {
            File[] files = recycleBin.listFiles();
            if (files != null && files.length > 10) { // Arbitrary threshold
                send(sender, "commands.delete.recycle-warning", Map.of("count", String.valueOf(files.length)));
                send(sender, "commands.delete.recycle-location", Map.of("path", recycleBin.getAbsolutePath()));
            }
        }
    }

    /**
     * Removes a world from the configuration file
     * @param worldName The name of the world to remove
     * @return true if the world was found and removed, false otherwise
     */
    private boolean removeFromConfig(String worldName) {
        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> worlds = config.getMapList("worlds");
        
        if (worlds.isEmpty()) {
            return false;
        }
        
        // Create a new list to avoid ConcurrentModificationException
        List<Map<?, ?>> updatedWorlds = new ArrayList<>(worlds);
        boolean found = false;
        
        // Find and remove the world with matching name
        for (Map<?, ?> world : worlds) {
            String name = (String) world.get("name");
            if (worldName.equalsIgnoreCase(name)) {
                updatedWorlds.remove(world);
                found = true;
                break;
            }
        }
        
        // If we found and removed the world, update the config
        if (found) {
            config.set("worlds", updatedWorlds);
            plugin.saveConfig();
            plugin.getLogger().info("Removed world '" + worldName + "' from configuration.");
        }
        
        return found;
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
        if (args.length == 1) {
            // Get worlds from config rather than just loaded worlds
            FileConfiguration config = plugin.getConfig();
            List<Map<?, ?>> worlds = config.getMapList("worlds");
            List<String> worldNames = new ArrayList<>();
            
            for (Map<?, ?> world : worlds) {
                String name = (String) world.get("name");
                if (name != null) {
                    worldNames.add(name);
                }
            }
            
            return worldNames;
        }
        return List.of();
    }
}
