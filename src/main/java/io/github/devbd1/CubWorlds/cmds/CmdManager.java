package io.github.devbd1.CubWorlds.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class CmdManager implements CommandExecutor, TabCompleter {
    private final Map<String, CmdInterface> commands = new HashMap<>();
    private final Map<String, String> aliasToCommand = new HashMap<>();

    public void register(CmdInterface cmd) {
        String name = cmd.getName().toLowerCase();
        commands.put(name, cmd);

        // Register aliases from config
        for (String alias : CmdConfig.getAliases(name)) {
            String lowerAlias = alias.toLowerCase();
            if (aliasToCommand.containsKey(lowerAlias)) {
                // Log warning about alias collision
                System.out.println("Warning: Alias '" + alias + "' is already registered, overwriting...");
            }
            aliasToCommand.put(lowerAlias, name);
        }
    }

    public Collection<CmdInterface> getCommands() {
        return commands.values();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7Use /cubDialogs <subCommand>");
            return true;
        }

        String subCmdName = args[0].toLowerCase();
        // Check if it's an alias
        if (aliasToCommand.containsKey(subCmdName)) {
            subCmdName = aliasToCommand.get(subCmdName);
        }

        CmdInterface sub = commands.get(subCmdName);
        if (sub == null) {
            sender.sendMessage("§cUnknown subcommand: " + args[0]);
            return true;
        }

        // Check permissions using both config and SubCommand interface
        if (!hasAnyPermission(sender, getEffectivePermissions(subCmdName, sub))) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        return sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            // Add main commands and their aliases if player has permission
            for (CmdInterface subCmd : commands.values()) {
                String cmdName = subCmd.getName().toLowerCase();
                if (hasAnyPermission(sender, getEffectivePermissions(cmdName, subCmd))) {
                    suggestions.add(subCmd.getName());
                    suggestions.addAll(CmdConfig.getAliases(subCmd.getName()));
                }
            }
            return suggestions;
        }

        String subCmdName = args[0].toLowerCase();
        if (aliasToCommand.containsKey(subCmdName)) {
            subCmdName = aliasToCommand.get(subCmdName);
        }

        CmdInterface sub = commands.get(subCmdName);
        if (sub == null || !hasAnyPermission(sender, getEffectivePermissions(subCmdName, sub))) {
            return Collections.emptyList();
        }

        List<String> result = sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Gets effective permissions by combining config permissions and SubCommand permissions
     */
    private List<String> getEffectivePermissions(String commandName, CmdInterface subCommand) {
        List<String> configPermissions = CmdConfig.getPermissions(commandName);
        List<String> interfacePermissions = subCommand.getPermissions();

        // Handle null safety
        if (configPermissions == null) configPermissions = Collections.emptyList();
        if (interfacePermissions == null) interfacePermissions = Collections.emptyList();

        // If config has permissions, use those (config takes priority)
        // Otherwise fall back to interface permissions
        return !configPermissions.isEmpty() ? configPermissions : interfacePermissions;
    }

    private boolean hasAnyPermission(CommandSender sender, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) return true;
        return permissions.stream().anyMatch(sender::hasPermission);
    }
}