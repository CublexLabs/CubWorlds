package io.github.cublexlabs.cubworlds.commands.sub;

import io.github.cublexlabs.cubworlds.Main;
import io.github.cublexlabs.cubworlds.commands.CmdConfig;
import io.github.cublexlabs.cubworlds.commands.CmdInterface;
import io.github.cublexlabs.cubworlds.hooks.LoggingManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
// import org.lushplugins.chatcolorhandler.ChatColorHandler

import java.lang.reflect.Method;
import java.util.List;

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
        logger.info("'/cubworlds <" + CMD_NAME + ">' command used by " + sender.getName());

        // VersionChecker checker = new VersionChecker();

        String name;
        String version;
        List<String> authors;

        // ChatColorHandler.sendMessage(sender, "§e§oChecking &6§oCubWorlds §e§oplugin version, please wait...");
        sender.sendMessage("§e§oChecking §6§oCubWorlds §e§oplugin version, please wait...");
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
        // ChatColorHandler.sendMessage(sender, "&6[" + name + "] &cversion: " + version);                                                                                                                                  │
        // ChatColorHandler.sendMessage(sender, "&6[" + name + "] &cauthors: " + String.join(", ", authors));
        sender.sendMessage("§eThis server is running &6" + name + " §eversion §6" + version + " §eby §6" + authors + "§e." + " (Implementing CublexAPI version &6" + version + "&e)");

        // ChatColorHandler.sendMessage(sender, "§eThis server is running &6" + name + " §eversion §6" + version + " §eby §6" + authors + "§e." + " (Implementing CublexAPI version &6" + version + "&e)")
        // Version check
        // checker.check(sender, "CublexLabs", name, version);

        // ChatColorHandler.sendMessage(sender, "Download the new version at: https://www.spigotmc.org/resources/cublexcore/");
        return true;
    }
}