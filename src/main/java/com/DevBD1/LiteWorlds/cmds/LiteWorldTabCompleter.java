package com.DevBD1.LiteWorlds.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TabCompleter implements TabCompleter{

    private final LiteWorldCommand command;

    public LiteWorldTabCompleter(LiteWorldCommand command) {
        this.command = command;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        Map<String, Subcommand> subcommands = command.getSubcommands();

        if (args.length == 1) {
            List<String> matches = new ArrayList<>();
            for (String name : subcommands.keySet()) {
                if (name.startsWith(args[0].toLowerCase())) {
                    matches.add(name);
                }
            }
            return matches;
        }

        Subcommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null) {
            return sub.tabComplete(sender, shiftArgs(args));
        }

        return Collections.emptyList();
    }

    private String[] shiftArgs(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args, 1, shifted, 0, args.length - 1);
        return shifted;
    }
}
