package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ListCmd implements CmdInterface {
    String CMD_NAME = "list";
    private final Main plugin;

    public ListCmd(Main plugin) {
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
        return "Lists all registered worlds and their status.";
    }

    @Override
    public String getUsage() {
        return "/kuksoworlds list";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> worlds = config.getMapList("worlds");

        if (worlds.isEmpty()) {
            send(sender, "commands.list.no-worlds");
            return false;
        }

        send(sender, "commands.list.header");
        for (Map<?, ?> world : worlds) {
            String name = (String) world.get("name");
            boolean isLoaded = Bukkit.getWorld(name) != null;
            // Note: Nested resolving might need Lang.t to return the status string first
            // But since Lang.t returns resolved string, we can't easily nest calls inside map unless we resolve them first.
            // I'll resolve status first.
            String statusKey = isLoaded ? "commands.list.status-loaded" : "commands.list.status-unloaded";
            String status;
            
            if (com.kukso.mc.lib.services.KuksoAPIProvider.isAvailable()) {
                com.kukso.mc.lib.services.LocalizationManager loc = com.kukso.mc.lib.services.KuksoAPIProvider.get().getLocalizationManager(plugin);
                if (sender instanceof org.bukkit.entity.Player player) {
                    status = loc.t(player, statusKey);
                } else {
                    // Fallback to Lang for console/non-player since API doesn't expose locale-based 't'
                    status = com.kukso.mc.lib.modules.text.Lang.t("en", statusKey, java.util.Collections.emptyMap());
                }
            } else {
                 status = isLoaded ? "LOADED" : "UNLOADED";
            }
            
            send(sender, "commands.list.entry", Map.of("name", name, "status", status));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
