package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class SetSpawnCmd implements CmdInterface {
    private static final String CMD_NAME = "setspawn";
    private final Main plugin;

    // basit bir confirmation sistemi için cache
    private Location pendingSpawn = null;

    public SetSpawnCmd(Main plugin) {
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
        return "Sets the spawn point of the current world.";
    }

    @Override
    public String getUsage() {
        return "/kuksoworlds setspawn [confirm]";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("confirm");
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, "general.only-players");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            if (pendingSpawn == null) {
                send(sender, "commands.setspawn.no-pending");
                return true;
            }
            applySpawn(player.getWorld(), pendingSpawn);
            send(sender, "commands.setspawn.success", Map.of("name", player.getWorld().getName()));
            pendingSpawn = null;
            return true;
        }

        // yeni spawn adayını al
        Location loc = player.getLocation();
        World world = player.getWorld();

        Location old = world.getSpawnLocation();
        pendingSpawn = loc.clone();

        send(sender, "commands.setspawn.confirm-header", Map.of("name", world.getName()));
        send(sender, "commands.setspawn.confirm-old", Map.of(
            "x", String.valueOf(old.getX()),
            "y", String.valueOf(old.getY()),
            "z", String.valueOf(old.getZ())
        ));
        send(sender, "commands.setspawn.confirm-new", Map.of(
            "x", String.valueOf(loc.getX()),
            "y", String.valueOf(loc.getY()),
            "z", String.valueOf(loc.getZ())
        ));
        send(sender, "commands.setspawn.confirm-hint");
        return true;
    }

    private void applySpawn(World world, Location loc) {
        // Bukkit tarafında ayarla
        world.setSpawnLocation(loc);

        // config tarafında güncelle
        List<Map<?, ?>> worlds = plugin.getConfig().getMapList("worlds");
        for (Map<?, ?> raw : worlds) {
            Object nameObj = raw.get("name");
            if (nameObj instanceof String worldName && worldName.equals(world.getName())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> entry = (Map<String, Object>) raw;
                Map<String, Object> spawn = new java.util.LinkedHashMap<>();
                spawn.put("x", loc.getX());
                spawn.put("y", loc.getY());
                spawn.put("z", loc.getZ());
                spawn.put("yaw", loc.getYaw());
                spawn.put("pitch", loc.getPitch());
                entry.put("spawn", spawn);
                break;
            }
        }
        plugin.getConfig().set("worlds", worlds);
        plugin.saveConfig();

        // 🔑 Burada WorldLoader cache'ini güncelle
        plugin.getWorldLoader().setSpawn(world.getName(), loc);
    }

}
