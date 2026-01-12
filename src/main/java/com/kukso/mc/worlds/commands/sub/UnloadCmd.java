package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

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
        return "/kuksoworlds unload <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            send(sender, "general.usage", Map.of("usage", getUsage()));
            return false;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            send(sender, "commands.unload.not-loaded", Map.of("name", worldName));
            return false;
        }

        FileConfiguration config = plugin.getConfig();
        String fallbackName = config.getString("fallback-world", "world");
        World fallback = Bukkit.getWorld(fallbackName);

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
            send(sender, "commands.unload.success", Map.of(
                "name", worldName,
                "count", String.valueOf(relocated),
                "fallback", fallbackName
            ));
        } else {
            send(sender, "commands.unload.failed", Map.of("name", worldName));
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}
