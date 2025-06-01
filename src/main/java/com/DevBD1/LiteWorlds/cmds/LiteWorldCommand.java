package com.DevBD1.LiteWorlds.cmds;

import com.DevBD1.LiteWorlds.Main;
import com.DevBD1.LiteWorlds.cmds.sub.CreateWorldSubcommand;
import com.DevBD1.LiteWorlds.cmds.sub.TeleportWorldSubcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class LiteWorldCommand implements CommandExecutor {
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public LiteWorldCommand(Main plugin) {
        registerSubcommand(new CreateWorldSubcommand(plugin));
        registerSubcommand(new TeleportWorldSubcommand());
    }

    private void registerSubcommand(SubCommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /" + label + " <subcommand>");
            return true;
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
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

    public Map<String, SubCommand> getSubcommands() {
        return subcommands;
    }
}