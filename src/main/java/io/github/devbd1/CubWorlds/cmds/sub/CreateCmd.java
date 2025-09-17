package io.github.devbd1.CubWorlds.cmds.sub;

import io.github.devbd1.CubWorlds.Main;
import io.github.devbd1.CubWorlds.cmds.CmdConfig;
import io.github.devbd1.CubWorlds.cmds.CmdInterface;
import io.github.devbd1.CubWorlds.generator.VoidWorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CreateCmd implements CmdInterface {
    String CMD_NAME = "create";
    private final Main plugin;
    private final Map<String, BukkitTask> progressTasks = new HashMap<>();

    public CreateCmd(Main plugin) {
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
        return "Creates a new world.";
    }

    @Override
    public String getUsage() {
        return "/cubworlds create <name> <type: NORMAL|VOID|NETHER|END> [prevent-grief:true|false]";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("NORMAL", "VOID", "NETHER", "END");
        }
        if (args.length == 3) {
            return Arrays.asList("prevent-grief:true", "prevent-grief:false");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            if (args.length < 2) {
                sender.sendMessage("Usage: " + getUsage());
                return false;
            }

            String name = args[0];
            
            // Check if world already exists
            if (Bukkit.getWorld(name) != null) {
                sender.sendMessage("§cWorld '" + name + "' already exists! Choose a different name.");
                return false;
            }
            
            String type = args[1].toUpperCase();
            boolean preventGrief = args.length >= 3 && args[2].equalsIgnoreCase("prevent-grief:true");

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
                    sender.sendMessage("§cInvalid world type. Use NORMAL, VOID, NETHER or END.");
                    return false;
                }
            }

            // Initial feedback
            sender.sendMessage("§aCreating world '" + name + "' with type '" + type + "'...");
            sender.sendMessage("§eThis may take a moment. Please wait while the world is being generated...");
            
            // Start a progress update task
            final long startTime = System.currentTimeMillis();
            BukkitTask progressTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                sender.sendMessage("§eStill creating world '" + name + "'... (" + elapsedSeconds + "s elapsed)");
            }, 100L, 200L); // First update after 5 seconds, then every 10 seconds
            
            progressTasks.put(name, progressTask);

            // Run world creation on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    long startCreateTime = System.currentTimeMillis();
                    sender.sendMessage("§eGenerating world structures. This could take several seconds...");
                    
                    // Create the world
                    Bukkit.createWorld(creator);
                    
                    // Cancel the progress task
                    BukkitTask task = progressTasks.remove(name);
                    if (task != null) {
                        task.cancel();
                    }
                    
                    // Calculate elapsed time
                    long totalTime = (System.currentTimeMillis() - startCreateTime) / 1000;
                    
                    // Success message
                    sender.sendMessage("§a✓ World '" + name + "' created successfully! (Took " + totalTime + " seconds)");
                    if (preventGrief) {
                        sender.sendMessage("§7Grief prevention is enabled for this world.");
                    }
                    sender.sendMessage("§7Use §f/cubworlds tp " + name + "§7 to teleport to the new world.");
                } catch (Exception e) {
                    // Cancel the progress task in case of error
                    BukkitTask task = progressTasks.remove(name);
                    if (task != null) {
                        task.cancel();
                    }
                    
                    sender.sendMessage("§c✗ Failed to create world: " + e.getMessage());
                    plugin.getLogger().severe("Error creating world " + name + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Update config asynchronously to avoid blocking
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    FileConfiguration config = plugin.getConfig();
                    List<Map<String, Object>> worlds = (List<Map<String, Object>>) config.getList("worlds");
                    if (worlds == null) worlds = new ArrayList<>();

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("name", name);
                    entry.put("type", type);
                    entry.put("prevent-grief", preventGrief);
                    worlds.add(entry);

                    config.set("worlds", worlds);
                    
                    // Save config on the main thread to avoid async issues
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.saveConfig();
                        plugin.getLogger().info("Added world '" + name + "' to config.yml");
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Error updating config for world " + name + ": " + e.getMessage());
                    sender.sendMessage("§cWarning: World was created but could not be saved to config.yml");
                }
            });
            
            return true;
        } catch (Exception e) {
            sender.sendMessage("§c✗ An unexpected error occurred: " + e.getMessage());
            plugin.getLogger().severe("Error in create command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}