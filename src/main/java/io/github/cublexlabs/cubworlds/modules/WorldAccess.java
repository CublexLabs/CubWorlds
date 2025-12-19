package io.github.cublexlabs.cubworlds.modules;

import io.github.cublexlabs.cubworlds.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Map;

public class WorldAccess implements Listener {
    private final Main plugin;

    public WorldAccess(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        check(event.getPlayer());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        check(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> check(event.getPlayer()));
    }

    private void check(Player player) {
        World world = player.getWorld();
        String fallbackName = plugin.getConfig().getString("fallback-world", "world");
        World fallback = Bukkit.getWorld(fallbackName);
        if (fallback == null || world.getName().equals(fallbackName)) return;

        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> worlds = config.getMapList("worlds");

        for (Map<?, ?> entry : worlds) {
            String name = (String) entry.get("name");
            String perm = (String) entry.get("permission");

            if (name == null || !name.equalsIgnoreCase(world.getName())) continue;
            if (perm != null && !perm.isBlank() && !player.hasPermission(perm)) {
                player.teleport(fallback.getSpawnLocation());
                break;
            }
        }
    }
}
