package io.github.cublexlabs.cubworlds.commands.sub;

import io.github.cublexlabs.cubworlds.Main;
import io.github.cublexlabs.cubworlds.commands.CmdConfig;
import io.github.cublexlabs.cubworlds.commands.CmdInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecycleCmd implements CmdInterface {
    String CMD_NAME = "recyclebin";
    private final Main plugin;

    public RecycleCmd(Main plugin) {
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
        return "Manage the deleted worlds in the recycle bin.";
    }

    @Override
    public String getUsage() {
        return "/cubworlds recyclebin [list|empty|restore <name>]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            File recycleBin = new File(Bukkit.getWorldContainer(), "recycle_bin");
            
            if (!recycleBin.exists()) {
                recycleBin.mkdirs();
                sender.sendMessage("§eRecycle bin is empty.");
                return true;
            }
            
            if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
                listRecycleBin(sender, recycleBin);
                return true;
            } else if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("empty")) {
                    emptyRecycleBin(sender, recycleBin);
                    return true;
                } else if (args[0].equalsIgnoreCase("restore") && args.length >= 2) {
                    restoreWorld(sender, recycleBin, args[1]);
                    return true;
                }
            }
            
            sender.sendMessage("§cUsage: " + getUsage());
            return false;
        } catch (Exception e) {
            sender.sendMessage("§cAn error occurred: " + e.getMessage());
            plugin.getLogger().severe("Error in recyclebin command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void listRecycleBin(CommandSender sender, File recycleBin) {
        File[] files = recycleBin.listFiles();
        
        if (files == null || files.length == 0) {
            sender.sendMessage("§eRecycle bin is empty.");
            return;
        }
        
        // Sort by modification time (newest first)
        Arrays.sort(files, Comparator.comparing(File::lastModified).reversed());
        
        sender.sendMessage("§6§lRecycle Bin Contents §7(" + files.length + " items):");
        
        // Group files by world name to show how many versions exist
        Map<String, List<File>> worldGroups = new HashMap<>();
        
        for (File file : files) {
            if (file.isDirectory()) {
                String name = file.getName();
                String worldName = name;
                
                // Extract the original world name if the file follows our naming convention
                if (name.contains("_")) {
//                    worldName = name.substring(0, name.indexOf('_'));
                    worldName = extractBaseName(file.getName());
                }
                
                // Group by world name
                if (!worldGroups.containsKey(worldName)) {
                    worldGroups.put(worldName, new ArrayList<>());
                }
                worldGroups.get(worldName).add(file);
            }
        }
        
        // Display world groups
        long totalSize = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Map.Entry<String, List<File>> entry : worldGroups.entrySet()) {
            String worldName = entry.getKey();
            List<File> versions = entry.getValue();
            
            // Calculate total size for this world (all versions)
            long worldTotalSize = versions.stream()
                    .mapToLong(this::calculateDirectorySize)
                    .sum();
            
            totalSize += worldTotalSize;
            
            // Display world name with count and total size
            sender.sendMessage("§e§l" + worldName + " §7(" + versions.size() + 
                    (versions.size() == 1 ? " version, " : " versions, ") + 
                    formatSize(worldTotalSize) + ")");
            
            // List each version with timestamp and size
            for (int i = 0; i < versions.size(); i++) {
                File version = versions.get(i);
                long size = calculateDirectorySize(version);
                
                // Extract timestamp if available
                String timestamp = "";
                String fileName = version.getName();
                if (fileName.contains("_")) {
                    String timepart = fileName.substring(fileName.indexOf('_') + 1);
                    // Replace underscores with spaces for better readability
                    timestamp = timepart.replace('_', ' ');
                }
                
                // Format the modified date as a fallback
                String modifiedDate = dateFormat.format(new Date(version.lastModified()));
                
                // Show version details
                if (versions.size() == 1) {
                    sender.sendMessage("  §7- §fDeleted on: §7" + 
                            (timestamp.isEmpty() ? modifiedDate : timestamp) + 
                            " §8(" + formatSize(size) + ")");
                } else {
                    sender.sendMessage("  §7" + (i + 1) + ". §fDeleted on: §7" + 
                            (timestamp.isEmpty() ? modifiedDate : timestamp) + 
                            " §8(" + formatSize(size) + ")");
                }
            }
        }
        
        sender.sendMessage("§7Total size: §f" + formatSize(totalSize));
        sender.sendMessage("§8§o(Use /cubworlds recyclebin empty to clear all)");
        sender.sendMessage("§8§o(Use /cubworlds recyclebin restore <name> to restore a world)");
    }
    
    private void emptyRecycleBin(CommandSender sender, File recycleBin) {
        File[] files = recycleBin.listFiles();
        
        if (files == null || files.length == 0) {
            sender.sendMessage("§eRecycle bin is already empty.");
            return;
        }
        
        int count = 0;
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    deleteDirectory(file.toPath());
                    count++;
                } else {
                    file.delete();
                    count++;
                }
            } catch (IOException e) {
                sender.sendMessage("§cFailed to delete " + file.getName() + ": " + e.getMessage());
            }
        }
        
        sender.sendMessage("§aRecycle bin emptied. Deleted " + count + " items.");
    }
    
    private void restoreWorld(CommandSender sender, File recycleBin, String worldName) {
        // First, check if a world with this name already exists
        if (Bukkit.getWorld(worldName) != null) {
            sender.sendMessage("§cA world with name '" + worldName + "' is already loaded. Cannot restore.");
            return;
        }
        
        File worldDir = new File(Bukkit.getWorldContainer(), worldName);
        if (worldDir.exists()) {
            sender.sendMessage("§cA world folder named '" + worldName + "' already exists. Cannot restore.");
            return;
        }
        
        // Find the world in the recycle bin
        File[] files = recycleBin.listFiles();
        if (files == null || files.length == 0) {
            sender.sendMessage("§cRecycle bin is empty.");
            return;
        }
        
        // Find all versions of this world
        List<File> worldVersions = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                String fileName = file.getName();
                if (fileName.equals(worldName) || fileName.startsWith(worldName + "_")) {
                    worldVersions.add(file);
                }
            }
        }
        
        if (worldVersions.isEmpty()) {
            sender.sendMessage("§cNo world named '" + worldName + "' found in the recycle bin.");
            return;
        }
        
        // If there's only one version, restore it directly
        if (worldVersions.size() == 1) {
            File worldToRestore = worldVersions.get(0);
            try {
                // Move the world back to the server directory
                Files.move(worldToRestore.toPath(), worldDir.toPath());
                sender.sendMessage("§aWorld '" + worldName + "' has been restored from the recycle bin.");
                sender.sendMessage("§7Use §f/cubworlds load " + worldName + "§7 to load it.");
                
                // Add world to config if it doesn't exist
                addRestoredWorldToConfig(sender, worldName);
            } catch (IOException e) {
                sender.sendMessage("§cFailed to restore world: " + e.getMessage());
                plugin.getLogger().severe("Error restoring world " + worldName + ": " + e.getMessage());
            }
            return;
        }
        
        // If there are multiple versions, sort by modified time (newest first) and use the newest
        worldVersions.sort(Comparator.comparing(File::lastModified).reversed());
        File newestVersion = worldVersions.get(0);
        
        try {
            // Move the world back to the server directory
            Files.move(newestVersion.toPath(), worldDir.toPath());
            sender.sendMessage("§aWorld '" + worldName + "' has been restored from the recycle bin.");
            sender.sendMessage("§7(Used the most recent version from " + 
                    new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(newestVersion.lastModified())) + ")");
            sender.sendMessage("§7Use §f/cubworlds load " + worldName + "§7 to load it.");
            
            // Add world to config if it doesn't exist
            addRestoredWorldToConfig(sender, worldName);
        } catch (IOException e) {
            sender.sendMessage("§cFailed to restore world: " + e.getMessage());
            plugin.getLogger().severe("Error restoring world " + worldName + ": " + e.getMessage());
        }
    }

    /**
     * Adds a restored world to the config if it doesn't already exist
     */
    private void addRestoredWorldToConfig(CommandSender sender, String worldName) {
        // Check if world already exists in config
        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> worlds = config.getMapList("worlds");
        
        boolean worldExists = false;
        for (Map<?, ?> world : worlds) {
            String name = (String) world.get("name");
            if (worldName.equals(name)) {
                worldExists = true;
                break;
            }
        }
        
        if (!worldExists) {
            // Add the world to config with default settings
            Map<String, Object> newWorld = new HashMap<>();
            newWorld.put("name", worldName);
            newWorld.put("type", "NORMAL"); // Default type
            newWorld.put("prevent-grief", false);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> typedWorlds = (List<Map<String, Object>>) (List<?>) worlds;
            typedWorlds.add(newWorld);
            
            config.set("worlds", typedWorlds);
            plugin.saveConfig();
            
            sender.sendMessage("§7Added world to configuration with default settings.");
            sender.sendMessage("§7(You may want to adjust its settings in config.yml)");
        }
    }
    
    private long calculateDirectorySize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += calculateDirectorySize(file);
                }
            }
        }
        
        return size;
    }
    
    private String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        
        if (size < sizeKb) {
            return size + " B";
        } else if (size < sizeMb) {
            return df.format(size / sizeKb) + " KB";
        } else if (size < sizeGb) {
            return df.format(size / sizeMb) + " MB";
        } else {
            return df.format(size / sizeGb) + " GB";
        }
    }
    
    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        // Log but continue with other files
                        plugin.getLogger().warning("Failed to delete " + p + ": " + e.getMessage());
                    }
                });
    }

    private String extractBaseName(String folderName) {
        int lastUnderscore = folderName.lastIndexOf('_');
        if (lastUnderscore == -1) return folderName;
        // Son alt çizgiden sonrası tamamen tarih mi diye kontrol edebilirsiniz
        String suffix = folderName.substring(lastUnderscore + 1);
        // Eğer suffix tamamen sayı+dash gibi bir pattern ise kes
        if (suffix.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            return folderName.substring(0, lastUnderscore);
        }
        return folderName; // güvenlik: pattern tutmazsa tüm isim
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("list", "empty", "restore");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("restore")) {
            // List world names available in recycle bin
            File recycleBin = new File(Bukkit.getWorldContainer(), "recycle_bin");
            if (recycleBin.exists() && recycleBin.isDirectory()) {
                File[] files = recycleBin.listFiles();
                if (files != null) {
                    return Arrays.stream(files)
                            .filter(File::isDirectory)
                            .map(file -> {
//                                String name = file.getName();
                                String name = extractBaseName(file.getName());
//                                if (name.contains("_")) {
//                                    return name.substring(0, name.indexOf('_'));
//                                }
                                return name;
                            })
                            .distinct()
                            .collect(Collectors.toList());
                }
            }
        }
        return List.of();
    }
}
