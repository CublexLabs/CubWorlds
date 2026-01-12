package com.kukso.mc.worlds.commands.sub;

import com.kukso.mc.worlds.Main;
import com.kukso.mc.worlds.commands.CmdConfig;
import com.kukso.mc.worlds.commands.CmdInterface;
import com.kukso.mc.worlds.hooks.LoggingManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
// import org.lushplugins.chatcolorhandler.ChatColorHandler

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class VersionCmd implements CmdInterface {
    String CMD_NAME = "version";
    private final Main plugin;
    private final LoggingManager logger;

    public VersionCmd(Main plugin) {
        this.plugin = plugin;
        this.logger = new LoggingManager(plugin);
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
        return CmdInterface.super.getDescription();
    }

    @Override
    public String getUsage() {
        return CmdInterface.super.getUsage();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        logger.info("'/kuksoworlds <" + CMD_NAME + ">' command used by " + sender.getName());

        String name;
        String version;
        List<String> authors;

        send(sender, "commands.version.checking");
        try {
            // Paper API (>=1.20.5) -> getPluginMeta()
            // Method metaMethod = JavaPlugin.class.getMethod("getPluginMeta")
            Method metaMethod = plugin.getClass().getMethod("getPluginMeta");
            Object pluginMeta = metaMethod.invoke(plugin);

            Method getName = pluginMeta.getClass().getMethod("getName");
            Method getVersion = pluginMeta.getClass().getMethod("getVersion");
            Method getAuthors = pluginMeta.getClass().getMethod("getAuthors");

            name = (String) getName.invoke(pluginMeta);
            version = (String) getVersion.invoke(pluginMeta);
            authors = (List<String>) getAuthors.invoke(pluginMeta);

        } catch (Exception e) {
            // Spigot still uses getDescription()
            PluginDescriptionFile desc = plugin.getDescription();

            name = desc.getName();
            version = desc.getVersion();
            authors = desc.getAuthors();
        }
        
        send(sender, "commands.version.info", Map.of(
            "name", name,
            "version", version,
            "authors", authors.toString()
        ));
        
        return true;
    }
}
