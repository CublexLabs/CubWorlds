package io.github.devbd1.CubWorlds.cmds.sub;

import io.github.devbd1.CubWorlds.Main;
import io.github.devbd1.CubWorlds.cmds.CmdConfig;
import io.github.devbd1.CubWorlds.cmds.CmdInterface;
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
        return "/cubworlds delete <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: " + getUsage());
                return false;
            }

            String worldName = args[0];
            World world = Bukkit.getWorld(worldName);
            FileConfiguration config = plugin.getConfig();
            String fallbackName = config.getString("fallback-world", "world");
            World fallback = Bukkit.getWorld(fallbackName);

            // Don't allow deleting the fallback world
            if (worldName.equals(fallbackName)) {
                sender.sendMessage("§cCannot delete the fallback world '" + fallbackName + "'.");
                return false;
            }

            // Teleport players if world is loaded
            if (world != null) {
                if (fallback == null) {
                    sender.sendMessage("§cFallback world '" + fallbackName + "' is not loaded. Cannot relocate players.");
                    return false;
                }

                int relocated = 0;
                for (Player player : world.getPlayers()) {
                    player.teleport(fallback.getSpawnLocation());
                    relocated++;
                }

                if (Bukkit.unloadWorld(world, true)) {
                    sender.sendMessage("§eWorld '" + worldName + "' unloaded. " + relocated + " player(s) relocated.");
                } else {
                    sender.sendMessage("§cFailed to unload world '" + worldName + "'. Aborting deletion.");
                    return false;
                }
            } else {
                sender.sendMessage("§eWorld '" + worldName + "' is not currently loaded. Proceeding with deletion.");
            }

            // Check if world folder exists
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (!worldFolder.exists()) {
                sender.sendMessage("§cWorld folder '" + worldName + "' does not exist on disk.");
                
                // Even if folder doesn't exist, we should still remove from config
                boolean removed = removeFromConfig(worldName);
                if (removed) {
                    sender.sendMessage("§eRemoved world '" + worldName + "' from configuration.");
                    return true;
                } else {
                    sender.sendMessage("§cWorld '" + worldName + "' not found in configuration.");
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
                    sender.sendMessage("§cAn unexpected conflict occurred in the recycle bin. Trying alternative method...");
                    deletedFolder = new File(recycleBinDir, worldName + "_" + timestamp + "_" + System.currentTimeMillis());
                }
                
                // Move the world folder to the recycle bin
                Files.move(worldFolder.toPath(), deletedFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sender.sendMessage("§aWorld folder '" + worldName + "' has been moved to recycle bin.");
                
                // Remove from config
                boolean removed = removeFromConfig(worldName);
                if (removed) {
                    sender.sendMessage("§aWorld '" + worldName + "' has been completely removed from the system.");
                    
                    // Check recycle bin size and warn if it's getting large
                    checkRecycleBinSize(sender);
                    
                    return true;
                } else {
                    sender.sendMessage("§eNote: World was deleted but was not found in configuration.");
                    return false;
                }
            } catch (IOException e) {
                sender.sendMessage("§cFailed to delete world folder '" + worldName + "': " + e.getMessage());
                plugin.getLogger().severe("Error deleting world " + worldName + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            sender.sendMessage("§cAn unexpected error occurred: " + e.getMessage());
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
                sender.sendMessage("§e⚠ Your recycle bin contains " + files.length + 
                        " deleted worlds. Consider emptying it to save disk space.");
                sender.sendMessage("§7The recycle bin is located at: " + recycleBin.getAbsolutePath());
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
