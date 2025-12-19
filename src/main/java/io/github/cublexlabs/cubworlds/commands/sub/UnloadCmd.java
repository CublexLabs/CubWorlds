package io.github.cublexlabs.cubworlds.commands.sub;

import io.github.cublexlabs.cubworlds.Main;
import io.github.cublexlabs.cubworlds.commands.CmdConfig;
import io.github.cublexlabs.cubworlds.commands.CmdInterface;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class UnloadCmd implements CmdInterface {
    String CMD_NAME = "unload";
    private final Main plugin;

    public UnloadCmd(Main plugin) {
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
        return "Unloads a world from memory.";
    }

    @Override
    public String getUsage() {
        return "/cubworlds unload <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return false;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage("World '" + worldName + "' is not currently loaded.");
            return false;
        }

        FileConfiguration config = plugin.getConfig();
        String fallbackName = config.getString("fallback-world", "world");
        World fallback = Bukkit.getWorld(fallbackName);

        if (fallback == null) {
            sender.sendMessage("Fallback world '" + fallbackName + "' is not loaded. Cannot relocate players.");
            return false;
        }

        int relocated = 0;
        for (Player player : world.getPlayers()) {
            player.teleport(fallback.getSpawnLocation());
            relocated++;
        }

        if (Bukkit.unloadWorld(world, true)) {
            sender.sendMessage("World '" + worldName + "' has been unloaded. " +
                    relocated + " player(s) relocated to '" + fallbackName + "'.");
        } else {
            sender.sendMessage("Failed to unload world '" + worldName + "'.");
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}
