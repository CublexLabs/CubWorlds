package io.github.devbd1.CubWorlds.listener;

import io.github.devbd1.CubWorlds.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class GriefPreventionListener implements Listener {

    private final List<String> protectedWorlds;

    public GriefPreventionListener(Main plugin) {
        FileConfiguration config = plugin.getConfig();
        protectedWorlds = config.getMapList("worlds").stream()
                .filter(m -> Boolean.TRUE.equals(m.get("prevent-grief")))
                .map(m -> (String) m.get("name"))
                .toList();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (protectedWorlds.contains(e.getBlock().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (protectedWorlds.contains(e.getBlock().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && protectedWorlds.contains(e.getClickedBlock().getWorld().getName())) {
            e.setCancelled(true);
        }
    }
}
