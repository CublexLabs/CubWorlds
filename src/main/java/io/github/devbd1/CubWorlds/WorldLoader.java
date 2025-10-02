package io.github.devbd1.CubWorlds;

import io.github.devbd1.CubWorlds.generator.VoidWorldGenerator;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import io.github.devbd1.CubWorlds.listener.GriefPreventionListener;
import io.github.devbd1.CubWorlds.listener.WorldAccessListener;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorldLoader {

    private final Plugin plugin;

    private final Map<String, Location> spawnLocations = new HashMap<>();

    public WorldLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reads a single world config entry, creates the world, and applies the
     * world-border, spawn, grief-prevention, and permission settings.
     *
     * @param worldData the single entry from the config (Map<String, Object>)
     */
    @SuppressWarnings("unchecked")
    public void loadWorld(Map<String, Object> worldData) {
        String name = (String) worldData.get("name");
        if (name == null || name.isBlank()) {
            plugin.getLogger().warning("Skipping world with missing name property.");
            return;
        }
        // --- Environment & Generator ---
        String type = ((String) worldData.getOrDefault("type", "NORMAL")).toUpperCase();
        WorldCreator creator = new WorldCreator(name);
        switch (type) {
            case "NETHER" -> creator.environment(World.Environment.NETHER);
            case "END" -> creator.environment(World.Environment.THE_END);
            case "VOID" -> {
                creator.environment(World.Environment.NORMAL);
                creator.generator(new VoidWorldGenerator());
            }
            default -> creator.environment(World.Environment.NORMAL);
        }
        // Bukkit world creation async–safe: create inside scheduler
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                World loadedWorld = Bukkit.createWorld(creator);
                if (loadedWorld == null) {
                    plugin.getLogger().severe("Failed to load world: " + name);
                    return;
                }
                plugin.getLogger().info("Loaded world: " + name);
                // === World Border ===
                Object wbObj = worldData.get("world-border");
                if (wbObj instanceof Map<?, ?> wbMapRaw) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> wbMap = (Map<String, Object>) wbMapRaw;

                    Object sizeObj = wbMap.getOrDefault("size", 0);
                    int size = (sizeObj instanceof Number nSize) ? nSize.intValue() : 0;

                    if (size > 0) {
                        WorldBorder border = loadedWorld.getWorldBorder();

                        double cx = 0, cz = 0;
                        Object centerObj = wbMap.get("center");
                        if (centerObj instanceof Map<?, ?> centerRaw) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> center = (Map<String, Object>) centerRaw;

                            Object xObj = center.getOrDefault("x", 0);
                            Object zObj = center.getOrDefault("z", 0);
                            cx = (xObj instanceof Number nX) ? nX.doubleValue() : 0;
                            cz = (zObj instanceof Number nZ) ? nZ.doubleValue() : 0;
                        }

                        border.setCenter(cx, cz);
                        border.setSize(size * 2.0);
                        //border.setWarningDistance(50);
                        //border.setDamageAmount(0.2);

                        plugin.getLogger().info("World-border applied to " + name + " radius=" + size);
                    }
                }
                // === Spawn ===
                Object spawnObj = worldData.get("spawn");
                Location spawnLoc;
                if (spawnObj instanceof Map<?, ?> spawnMap) {
                    Map<?, ?> s = spawnMap;

                    double x = s.get("x") instanceof Number ? ((Number) s.get("x")).doubleValue() : 0.0;
                    double y = s.get("y") instanceof Number ? ((Number) s.get("y")).doubleValue()
                            : loadedWorld.getHighestBlockYAt(0, 0) + 1;
                    double z = s.get("z") instanceof Number ? ((Number) s.get("z")).doubleValue() : 0.0;
                    float yaw = s.get("yaw") instanceof Number ? ((Number) s.get("yaw")).floatValue() : 0f;
                    float pitch = s.get("pitch") instanceof Number ? ((Number) s.get("pitch")).floatValue() : 0f;

                    // World#setSpawnLocation floora yuvarlar, ama location'ı ayrıca tutacam
                    loadedWorld.setSpawnLocation(new Location(loadedWorld,
                            Math.floor(x), Math.floor(y), Math.floor(z), yaw, pitch));

                    // Tam kesirli konumu ayrı sakliyorum teleportta kullanmk icin
                    spawnLoc = new Location(loadedWorld, x, y, z, yaw, pitch);
                } else {
                    // config yoksa default spawn
                    spawnLoc = loadedWorld.getSpawnLocation();
                }

                spawnLocations.put(name.toLowerCase(Locale.ROOT), spawnLoc);
                plugin.getLogger().info("World loaded: " + name + " spawn saved.");


//                // === Prevent-Grief ===
//                if (Boolean.TRUE.equals(worldData.get("prevent-grief"))) {
//                    GriefPreventionListener.registerProtectedWorld(name);
//                }
//
//                // === Permission ===
//                String permission = (String) worldData.get("permission");
//                if (permission != null && !permission.isBlank()) {
//                    WorldAccessListener.registerWorldPermission(name, permission);
//                }

            } catch (Exception ex) {
                plugin.getLogger().severe("Error while loading world " + name + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 1L);

    }
    /**
     * Diğer class’lar bu metotla spawn location’a ulaşıcak.
     */
    public Location getSpawn(String worldName) {
        return spawnLocations.get(worldName.toLowerCase(Locale.ROOT));
    }
    public void setSpawn(String worldName, Location loc) {
        spawnLocations.put(worldName.toLowerCase(Locale.ROOT), loc.clone());
    }

}
