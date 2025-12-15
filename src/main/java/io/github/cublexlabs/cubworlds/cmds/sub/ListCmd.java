package io.github.cublexlabs.cubworlds.cmds.sub;

import io.github.cublexlabs.cubworlds.Main;
import io.github.cublexlabs.cubworlds.cmds.CmdConfig;
import io.github.cublexlabs.cubworlds.cmds.CmdInterface;
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
        return "/cubworlds list";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        FileConfiguration config = plugin.getConfig();
        List<Map<?, ?>> worlds = config.getMapList("worlds");

        if (worlds.isEmpty()) {
            sender.sendMessage("No worlds registered in config.");
            return false;
        }

        sender.sendMessage("Registered worlds:");
        for (Map<?, ?> world : worlds) {
            String name = (String) world.get("name");
            boolean isLoaded = Bukkit.getWorld(name) != null;
            sender.sendMessage("- " + name + " (" + (isLoaded ? "LOADED" : "UNLOADED") + ")");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}