package io.github.devbd1.CubWorlds.cmds.sub;

import io.github.devbd1.CubWorlds.cmds.CmdConfig;
import io.github.devbd1.CubWorlds.cmds.CmdInterface;
import io.github.devbd1.CubWorlds.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeleportCmd implements CmdInterface {
    String CMD_NAME = "teleport";
    private final Main plugin;

    public TeleportCmd(Main plugin) {
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
        return "Teleports to a world spawn.";
    }

    @Override
    public String getUsage() {
        return "/cubworlds teleport <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can teleport.");
            return false;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: " + getUsage());
            return false;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage("World is not loaded. Load it first using /liteworld load " + worldName);
            return false;
        }

        player.teleport(world.getSpawnLocation());
        sender.sendMessage("Teleported to world: " + worldName);
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}