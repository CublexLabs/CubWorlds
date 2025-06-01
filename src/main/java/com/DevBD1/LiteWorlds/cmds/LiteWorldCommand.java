package com.DevBD1.LiteWorlds.cmds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public CommandManager() {
        registerSubcommand(new CreateWorldSubcommand());
        registerSubcommand(new TeleportWorldSubcommand());
    }

    private void registerSubcommand(Subcommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <subcommand>");
            return true;
        }

        Subcommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null) {
            sub.execute(sender, shiftArgs(args));
        } else {
            sender.sendMessage("Unknown subcommand. Use /" + label + " help");
        }
        return true;
    }

    private String[] shiftArgs(String[] args) {
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args, 1, shifted, 0, args.length - 1);
        return shifted;
    }

    public Map<String, Subcommand> getSubcommands() {
        return subcommands;
    }
}