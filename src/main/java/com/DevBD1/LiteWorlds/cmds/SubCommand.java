package com.DevBD1.LiteWorlds.cmds;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName();
    String getDescription();
    String getUsage();
    void execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
}