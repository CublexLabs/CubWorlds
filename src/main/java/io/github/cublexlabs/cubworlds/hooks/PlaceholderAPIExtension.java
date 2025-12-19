
package io.github.cublexlabs.cubworlds.hooks;

import io.github.cublexlabs.cubworlds.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * PlaceholderAPI extension for CubWorlds plugin.
 * Provides placeholders for world information and player statistics.
 */
public class PlaceholderAPIExtension extends PlaceholderExpansion {

    private final Main plugin;
    private static final boolean DEBUG_MODE = true; // Set to false in production

    public PlaceholderAPIExtension(Main plugin) {
        this.plugin = plugin;
        debug("PlaceholderAPIExtension initialized");
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "cubworlds";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Keep registered when PlaceholderAPI reloads
    }

    @Override
    @Nullable
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        debug("Placeholder request - Player: " + (player != null ? player.getName() : "null")
                + ", Params: " + params);

        // Validate player
        if (player == null) {
            debug("Request failed: Player is null");
            return null;
        }

        // Parse placeholder using switch for better performance
        String result = processPlaceholder(player, params.toLowerCase());

        debug("Placeholder result - " + params + " = " + (result != null ? result : "null"));
        return result;
    }

    /**
     * Process the placeholder request based on the parameter.
     *
     * @param player The player requesting the placeholder
     * @param params The placeholder parameter (lowercase)
     * @return The placeholder value or null if not found
     */
    @Nullable
    private String processPlaceholder(OfflinePlayer player, String params) {
        // Handle dynamic placeholders with parameters (e.g., world_spawnloc_world_name)
        if (params.startsWith("world_spawnloc_")) {
            String worldName = params.substring("world_spawnloc_".length());
            return getWorldSpawnLocation(worldName);
        }
        switch (params) {
            case "world_name":
                return getWorldName(player);

            case "world_loaded":
                return getWorldLoadedStatus(player);

            case "chunks_unlocked":
                return getChunksUnlocked(player);

            case "player_world_spawn":
                return getPlayerWorldSpawn(player);

            case "loaded_worlds_count":
                return getLoadedWorldsCount();

            // Legacy/config-based placeholder (consider removing if unused)
            case "placeholder1":
                return plugin.getConfig().getString("placeholders.placeholder1", "default1");

            default:
                debug("Unknown placeholder: " + params);
                return null;
        }
    }

    /**
     * Get the name of the world the player is currently in.
     * Requires player to be online.
     *
     * @param player The player
     * @return World name or error message
     */
    @NotNull
    private String getWorldName(OfflinePlayer player) {
        if (!player.isOnline()) {
            debug("world_name failed: Player is offline");
            return "N/A";
        }

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            debug("world_name failed: Player object is null");
            return "N/A";
        }

        Location location = onlinePlayer.getLocation();
        if (location == null) {
            debug("world_name failed: Location is null");
            return "N/A";
        }

        World world = location.getWorld();
        if (world == null) {
            debug("world_name failed: World is null");
            return "Unknown";
        }

        return world.getName();
    }

    /**
     * Check if the player's current world is loaded by CubWorlds.
     *
     * @param player The player
     * @return "true" or "false"
     */
    @NotNull
    private String getWorldLoadedStatus(OfflinePlayer player) {
        if (!player.isOnline() || player.getPlayer() == null) {
            debug("world_loaded failed: Player is offline");
            return "false";
        }

        Player onlinePlayer = player.getPlayer();
        World world = onlinePlayer.getLocation().getWorld();

        if (world == null) {
            debug("world_loaded failed: World is null");
            return "false";
        }

        // Check if world is managed by CubWorlds
        boolean isLoaded = plugin.getWorldLoader() != null
                && plugin.getWorldLoader().isWorldLoaded(world.getName());

        debug("world_loaded check: " + world.getName() + " = " + isLoaded);
        return String.valueOf(isLoaded);
    }

    /**
     * Get the number of chunks unlocked for the player.
     * TODO: Implement actual chunk tracking system
     *
     * @param player The player
     * @return Number of unlocked chunks as string
     */
    @NotNull
    private String getChunksUnlocked(OfflinePlayer player) {
        // TODO: Replace with actual CubWorlds API call when implemented
        // Example: return String.valueOf(plugin.getChunkManager().getUnlockedChunks(player));

        debug("chunks_unlocked: Returning test data (not implemented)");
        return "0"; // Changed from "12" to "0" to indicate unimplemented
    }

    /**
     * Get the spawn location of the player's current world.
     *
     * @param player The player
     * @return Formatted spawn coordinates
     */
    @NotNull
    private String getPlayerWorldSpawn(OfflinePlayer player) {
        if (!player.isOnline() || player.getPlayer() == null) {
            debug("player_world_spawn failed: Player is offline");
            return "N/A";
        }

        Player onlinePlayer = player.getPlayer();
        World world = onlinePlayer.getLocation().getWorld();

        if (world == null) {
            debug("player_world_spawn failed: World is null");
            return "N/A";
        }

        Location spawn = world.getSpawnLocation();
        return String.format("%d, %d, %d", spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
    }

    /**
     * Get the spawn location of a specific world by name.
     * This uses the CubWorlds custom spawn if available, otherwise falls back to Bukkit's spawn.
     *
     * @param worldName The name of the world
     * @return Formatted spawn coordinates (with decimal precision) or error message
     */
    @NotNull
    private String getWorldSpawnLocation(String worldName) {
        debug("world_spawn requested for: " + worldName);

        if (worldName == null || worldName.isBlank()) {
            debug("world_spawn failed: World name is null or blank");
            return "Invalid World";
        }

        // Check if WorldLoader exists
        if (plugin.getWorldLoader() == null) {
            debug("world_spawn failed: WorldLoader is null");
            return "N/A";
        }

        // Try to get custom spawn from CubWorlds configuration
        Location customSpawn = plugin.getWorldLoader().getSpawn(worldName);

        if (customSpawn != null) {
            debug("world_spawn: Using CubWorlds custom spawn for " + worldName);
            // Return with decimal precision for exact coordinates
            return String.format("%.1f, %.1f, %.1f",
                    customSpawn.getX(),
                    customSpawn.getY(),
                    customSpawn.getZ()
            );
        }

        // Fallback to Bukkit's default spawn if world exists but no custom spawn
        World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world != null) {
            debug("world_spawn: Using Bukkit default spawn for " + worldName);
            Location bukkitSpawn = world.getSpawnLocation();
            return String.format("%d, %d, %d",
                    bukkitSpawn.getBlockX(),
                    bukkitSpawn.getBlockY(),
                    bukkitSpawn.getBlockZ()
            );
        }

        debug("world_spawn failed: World '" + worldName + "' not found");
        return "World Not Found";
    }

    /**
     * Get the count of loaded CubWorlds.
     *
     * @return Number of loaded worlds as string
     */
    @NotNull
    private String getLoadedWorldsCount() {
        if (plugin.getWorldLoader() == null) {
            debug("loaded_worlds_count failed: WorldLoader is null");
            return "0";
        }

        int count = plugin.getWorldLoader().getLoadedWorldsCount();
        debug("loaded_worlds_count: " + count);
        return String.valueOf(count);
    }

    /**
     * Log debug messages if debug mode is enabled.
     *
     * @param message The debug message
     */
    private void debug(String message) {
        if (DEBUG_MODE) {
            plugin.getLogger().log(Level.INFO, "[PlaceholderAPI Debug] " + message);
        }
    }
}