package io.github.cublexlabs.cubworlds.modules;

import io.github.cublexlabs.cubworlds.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Handles grief prevention by restricting block modifications and interactions in specific worlds.
 *
 * <p>This listener identifies worlds defined in the plugin configuration where the
 * {@code prevent-grief} flag is set to true. Events such as breaking blocks,
 * placing blocks, and player interactions are cancelled in these worlds to preserve state.
 *
 * <p>Expected configuration structure:
 * <pre>
 * worlds:
 * - name: "hub_world"
 *   prevent-grief: true
 * </pre>
 *
 * @author DevBD1
 * @version 2.0
 * @since 1.0
 * @see io.github.cublexlabs.cubworlds.Main
 */
public class GriefPrevention implements Listener {

    private final List<String> protectedWorlds;

    public GriefPrevention(Main plugin) {
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
