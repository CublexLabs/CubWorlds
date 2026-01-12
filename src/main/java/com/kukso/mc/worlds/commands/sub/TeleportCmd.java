package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.WorldLoader;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TeleportCmd implements CmdInterface {
    private static final String CMD_NAME = "teleport";
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
        return "/kuksoworlds teleport <worldName>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "general.only-players");
            return true;
        }

        if (args.length < 1) {
            send(sender, "general.usage", Map.of("usage", getUsage()));
            return true;
        }

        String worldName = args[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            send(sender, "general.world-not-found", Map.of("name", worldName));
            return true;
        }

        // ✅ WorldLoader’dan config tabanlı spawn çek
        WorldLoader loader = plugin.getWorldLoader();
        Location customSpawn = loader.getSpawn(worldName);

        if (customSpawn == null) {
            // Config’te spawn yoksa Bukkit default spawn’a düş
            customSpawn = world.getSpawnLocation();
        }

        player.teleport(customSpawn);
        send(sender, "commands.teleport.success", Map.of("name", worldName));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Bukkit.getWorlds().stream().map(World::getName).toList();
    }
}
