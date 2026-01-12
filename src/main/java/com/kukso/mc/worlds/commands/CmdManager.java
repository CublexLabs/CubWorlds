package com.kukso.mc.worlds.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

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
            // sender.sendMessage("§7Use /kuksoworlds <subCommand>");
            // Using a generic help message or relying on usage from main command if defined
            // For now, mapping to unknown subcommand or usage
            sender.sendMessage("§7Use /kuksoworlds <subCommand>"); 
            return true;
        }

        String subCmdName = args[0].toLowerCase();
        // Check if it's an alias
        if (aliasToCommand.containsKey(subCmdName)) {
            subCmdName = aliasToCommand.get(subCmdName);
        }

        CmdInterface sub = commands.get(subCmdName);
        if (sub == null) {
            sub = commands.get("help"); // Fallback if help exists, otherwise error
            if (sub == null) {
                // Manually sending localized message since we don't have a sub-command instance to call send() on easily 
                // without casting or static helper. 
                // But CmdInterface.send is default method, need an instance. 
                // I'll stick to manual Lang.t call here.
                String msg;
                Map<String, String> ph = Map.of("command", args[0]);
                if (sender instanceof org.bukkit.entity.Player player) {
                    msg = com.kukso.mc.lib.modules.text.Lang.t(player, "general.unknown-subcommand", ph);
                } else {
                    msg = com.kukso.mc.lib.modules.text.Lang.t("en", "general.unknown-subcommand", ph);
                }
                sender.sendMessage(msg);
                return true;
            }
        }

        // Check permissions using both config and SubCommand interface
        if (!hasAnyPermission(sender, getEffectivePermissions(subCmdName, sub))) {
             String msg;
             if (sender instanceof org.bukkit.entity.Player player) {
                 msg = com.kukso.mc.lib.modules.text.Lang.t(player, "general.no-permission");
             } else {
                 msg = com.kukso.mc.lib.modules.text.Lang.t("en", "general.no-permission", Collections.emptyMap());
             }
             sender.sendMessage(msg);
             return true;
        }

        return sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            // This is the first argument (subcommand selection)
            List<String> suggestions = new ArrayList<>();
            String partialInput = args[0].toLowerCase();
            
            // Add main commands and their aliases if player has permission
            for (CmdInterface subCmd : commands.values()) {
                String cmdName = subCmd.getName().toLowerCase();
                if (hasAnyPermission(sender, getEffectivePermissions(cmdName, subCmd))) {
                    // Add the main command name if it matches the partial input
                    if (cmdName.startsWith(partialInput)) {
                        suggestions.add(subCmd.getName());
                    }
                    
                    // Add aliases that match the partial input
                    for (String alias1 : CmdConfig.getAliases(subCmd.getName())) {
                        if (alias1.toLowerCase().startsWith(partialInput)) {
                            suggestions.add(alias1);
                        }
                    }
                }
            }
            
            return suggestions;
        }

        // For subsequent arguments, pass to the appropriate subcommand
        String subCmdName = args[0].toLowerCase();
        if (aliasToCommand.containsKey(subCmdName)) {
            subCmdName = aliasToCommand.get(subCmdName);
        }

        CmdInterface sub = commands.get(subCmdName);
        if (sub == null || !hasAnyPermission(sender, getEffectivePermissions(subCmdName, sub))) {
            return Collections.emptyList();
        }

        // Get suggestions from the subcommand's tabComplete method
        List<String> result = sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        
        // Filter the results based on what the user has typed so far
        if (result != null && !result.isEmpty() && args.length > 1) {
            String lastArg = args[args.length - 1].toLowerCase();
            return result.stream()
                    .filter(suggestion -> suggestion.toLowerCase().startsWith(lastArg))
                    .collect(Collectors.toList());
        }
        
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
