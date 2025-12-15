package io.github.cublexlabs.cubworlds.cmds;

import io.github.cublexlabs.cubworlds.Main;
import io.github.cublexlabs.cubworlds.cmds.sub.*;
import io.github.devbd1.CubWorlds.cmds.sub.*;
import io.github.devbd1.cublexcore.modules.logger.LoggingManager;
import org.bukkit.command.PluginCommand;

import java.util.logging.Level;

public class CmdRegistrar {

    private CmdRegistrar() {}

    public static void register(Main plugin) {

        try {
            PluginCommand cmd = plugin.getCommand("CubWorlds");
            
            if (cmd == null) {
                // Try with lowercase as a fallback
                cmd = plugin.getCommand("cubworlds");
            }
            
            if (cmd == null) {
                plugin.getLogger().severe("COMMAND 'CubWorlds' NOT FOUND! Make sure it's properly defined in plugin.yml.");
                plugin.getLogger().severe("Commands will not be available. Check plugin.yml for proper command registration.");
                
                // Log the available commands for debugging
                plugin.getLogger().info("Available commands registered in this plugin:");
                plugin.getDescription().getCommands().keySet().forEach(command -> 
                    plugin.getLogger().info("- " + command)
                );
                
                return;
            }
            
            plugin.getLogger().info("Registering commands for CubWorlds...");
            
            CmdManager mgr = new CmdManager();

            // 🔹 CublexCore bağlantısı
            io.github.devbd1.cublexcore.Main core =
                    (io.github.devbd1.cublexcore.Main) plugin.getServer().getPluginManager().getPlugin("CublexCore");

            if (core == null || !core.isEnabled()) {
                plugin.getLogger().severe("CublexCore not found or not enabled! Skipping VersionCmd registration.");
            } else {
                LoggingManager logger = core.getLoggingManager();
                try {
                    mgr.register(new VersionCmd(plugin, logger));
                    plugin.getLogger().info("Registered command: version");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to register VersionCmd: " + e.getMessage(), e);
                }
            }

            try {
                mgr.register(new RecycleCmd(plugin));
                plugin.getLogger().info("Registered command: recycle");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register RecycleCmd: " + e.getMessage(), e);
            }

            try {
                mgr.register(new CreateCmd(plugin));
                plugin.getLogger().info("Registered command: create");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register CreateCmd: " + e.getMessage(), e);
            }
            
            try {
                mgr.register(new DeleteCmd(plugin));
                plugin.getLogger().info("Registered command: delete");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register DeleteCmd: " + e.getMessage(), e);
            }
            
            try {
                mgr.register(new ListCmd(plugin));
                plugin.getLogger().info("Registered command: list");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register ListCmd: " + e.getMessage(), e);
            }
            
            try {
                mgr.register(new LoadCmd(plugin));
                plugin.getLogger().info("Registered command: load");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register LoadCmd: " + e.getMessage(), e);
            }
            
            try {
                mgr.register(new TeleportCmd(plugin));
                plugin.getLogger().info("Registered command: teleport");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register TeleportCmd: " + e.getMessage(), e);
            }
            
            try {
                mgr.register(new UnloadCmd(plugin));
                plugin.getLogger().info("Registered command: unload");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register UnloadCmd: " + e.getMessage(), e);
            }

            try {
                mgr.register(new SetSpawnCmd(plugin));
                plugin.getLogger().info("Registered command: unload");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register UnloadCmd: " + e.getMessage(), e);
            }

            cmd.setExecutor(mgr);
            cmd.setTabCompleter(mgr);
            plugin.getLogger().info("Command registration complete. Commands should be available via /cubworlds or /cw");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error during command registration: " + e.getMessage(), e);
        }
    }
}
