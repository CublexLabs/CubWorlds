package com.kukso.mc.worlds.commands;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.sub.*;
//import io.github.cublexlabs.cublexcore.modules.logger.LoggingManager;
import org.bukkit.command.PluginCommand;

import java.util.logging.Level;

public class CmdRegistrar {

    private CmdRegistrar() {}

    public static void register(Main plugin) {

        try {
            PluginCommand cmd = plugin.getCommand("kuksoworlds");
            
            if (cmd == null) {
                // Try with lowercase as a fallback
                cmd = plugin.getCommand("kuksoworlds");
            }
            
            if (cmd == null) {
                plugin.getLogger().severe("COMMAND 'kuksoworlds' NOT FOUND! Make sure it's properly defined in plugin.yml.");
                plugin.getLogger().severe("Commands will not be available. Check plugin.yml for proper command registration.");
                
                // Log the available commands for debugging
                plugin.getLogger().info("Available commands registered in this plugin:");
                plugin.getDescription().getCommands().keySet().forEach(command -> 
                    plugin.getLogger().info("- " + command)
                );
                
                return;
            }
            
            plugin.getLogger().info("Registering commands for KuksoWorlds...");
            
            CmdManager mgr = new CmdManager();

            try {
                //mgr.register(new VersionCmd(plugin));
                mgr.register(new VersionCmd(plugin));
                plugin.getLogger().info("Registered command: version");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register VersionCmd: " + e.getMessage(), e);
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
                plugin.getLogger().info("Registered command: setspawn");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to register SetSpawnCmd: " + e.getMessage(), e);
            }

            cmd.setExecutor(mgr);
            cmd.setTabCompleter(mgr);
            plugin.getLogger().info("Command registration complete. Commands should be available via /kuksoworlds or /kw");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error during command registration: " + e.getMessage(), e);
        }
    }
}
