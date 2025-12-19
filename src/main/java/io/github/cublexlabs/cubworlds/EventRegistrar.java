package io.github.cublexlabs.cubworlds;

import io.github.cublexlabs.cubworlds.modules.GriefPrevention;
import io.github.cublexlabs.cubworlds.modules.WorldAccess;
import org.bukkit.Server;

public class EventRegistrar {
    public static void register(Server server, Main plugin) {
        server.getPluginManager().registerEvents(new GriefPrevention(plugin), plugin);
        server.getPluginManager().registerEvents(new WorldAccess(plugin), plugin);
    }
}
