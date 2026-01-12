package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import com.kukso.mc.worlds.generator.VoidWorldGenerator;
import com.kukso.mc.lib.modules.logging.PlayerLogger;
import com.kukso.mc.lib.modules.logging.ServerLogger;
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
    private final ServerLogger serverLogger;
    private final PlayerLogger playerLogger;

    public CreateCmd(Main plugin) {
        this.plugin = plugin;
        this.serverLogger = new ServerLogger(plugin);
        this.playerLogger = new PlayerLogger(plugin, this.serverLogger);
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
        //return "/kuksoworlds create <name> <type: NORMAL|VOID|NETHER|END> [prevent-grief:true|false]";
        return "/kuksoworlds create <name> <NORMAL|VOID|NETHER|END> [prevent-grief:<true|false>] [world-border:<number>]";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Arrays.asList("NORMAL", "VOID", "NETHER", "END");
        }
        if (args.length == 3) {
            return Arrays.asList("prevent-grief:true", "prevent-grief:false");
        }
        if (args.length == 4 && args[2].toLowerCase().startsWith("prevent-grief")) {
            return Arrays.asList("world-border:1000");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            if (args.length < 2) {
                send(sender, "general.usage", Map.of("usage", getUsage()));
                return false;
            }

            String name = args[0];

            if (Bukkit.getWorld(name) != null) {
                send(sender, "commands.create.exists", Map.of("name", name));
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
                    send(sender, "commands.create.invalid-type");
                    return false;
                }
            }

            Integer worldBorderSize = null;
            for (int i = 2; i < args.length; i++) {
                String a = args[i].toLowerCase();
                if (a.startsWith("prevent-grief:")) {
                    preventGrief = a.equalsIgnoreCase("prevent-grief:true");
                } else if (a.startsWith("world-border:")) {
                    try {
                        worldBorderSize = Integer.parseInt(a.split(":", 2)[1]);
                    } catch (NumberFormatException ex) {
                        send(sender, "commands.create.invalid-border");
                        return false;
                    }
                }
            }

            // Initial feedback
            send(sender, "commands.create.start", Map.of("name", name, "type", type));
            send(sender, "commands.create.wait");
            
            // Start a progress update task
            final long startTime = System.currentTimeMillis();
            BukkitTask progressTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                send(sender, "commands.create.still-creating", Map.of("name", name, "time", String.valueOf(elapsedSeconds)));
            }, 100L, 200L); // First update after 5 seconds, then every 10 seconds
            
            progressTasks.put(name, progressTask);

            // Run world creation on the main thread
            Integer finalWorldBorderSize = worldBorderSize;
            boolean finalPreventGrief = preventGrief;
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    long startCreateTime = System.currentTimeMillis();
                    send(sender, "commands.create.generating-structures");
                    
                    // Create the world
                    Bukkit.createWorld(creator);

                    org.bukkit.World world = Bukkit.getWorld(name);
                    if (world != null && finalWorldBorderSize != null) {
                        org.bukkit.WorldBorder border = world.getWorldBorder();
                        border.setCenter(world.getSpawnLocation()); // spawn merkezli
                        border.setSize(finalWorldBorderSize);
                        border.setCenter(0,0);
                        // opsiyonel: warning / damage
                        border.setWarningDistance(50);
                        border.setDamageAmount(0.2);
                        send(sender, "commands.create.border-set", Map.of("size", String.valueOf(finalWorldBorderSize)));
                    }


                    // Cancel the progress task
                    BukkitTask task = progressTasks.remove(name);
                    if (task != null) {
                        task.cancel();
                    }
                    
                    // Calculate elapsed time
                    long totalTime = (System.currentTimeMillis() - startCreateTime) / 1000;
                    
                    // Success message
                    send(sender, "commands.create.success", Map.of("name", name, "time", String.valueOf(totalTime)));
                    
                    // Logging
                    serverLogger.log("World '" + name + "' created successfully by " + sender.getName());
                    if (sender instanceof org.bukkit.entity.Player player) {
                        playerLogger.logPlayer(player, "Created world '" + name + "'");
                    }

                    if (finalPreventGrief) {
                        send(sender, "commands.create.grief-prevention");
                    }
                    send(sender, "commands.create.teleport-hint", Map.of("name", name));
                } catch (Exception e) {
                    // Cancel the progress task in case of error
                    BukkitTask task = progressTasks.remove(name);
                    if (task != null) {
                        task.cancel();
                    }
                    
                    send(sender, "commands.create.failed", Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
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
                    entry.put("prevent-grief", finalPreventGrief);
                    entry.put("world-border", finalWorldBorderSize != null ? finalWorldBorderSize : 0);
                    worlds.add(entry);

                    config.set("worlds", worlds);
                    
                    // Save config on the main thread to avoid async issues
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.saveConfig();
                        plugin.getLogger().info("Added world '" + name + "' to config.yml");
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("Error updating config for world " + name + ": " + e.getMessage());
                    send(sender, "commands.create.config-warning");
                }
            });
            
            return true;
        } catch (Exception e) {
            send(sender, "general.unexpected-error", Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
            plugin.getLogger().severe("Error in create command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
