package com.minecade.rfb.command;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.minecade.engine.MinecadePlugin;
import com.minecade.engine.utils.MinecadeCommand;
import com.minecade.rfb.plugin.RunFromTheBeastPlugin;

@MinecadeCommand(commandName="beast")
public class BeastCommand implements CommandExecutor {

    private final MinecadePlugin plugin;

    public BeastCommand(MinecadePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.plugin.getServer().getLogger().info("beastcommand");
        if(plugin instanceof RunFromTheBeastPlugin){
            if (sender instanceof Player) {
                // Only CM, GM , Admin and OP players can call this command.
                Player bukkitPlayer = (Player) sender;
                
                if (plugin.getPersistence().isPlayerStaff(bukkitPlayer)) {
                    String response = ((RunFromTheBeastPlugin)plugin).beBeast(bukkitPlayer);
                    if(StringUtils.isBlank(response)) {
                        sender.sendMessage(String.format("%sSuccess!%s You will be the beast in the match!", ChatColor.YELLOW, ChatColor.GREEN ));
                    } else {
                        sender.sendMessage(String.format("%sYou can't be the beast, error message: %s", ChatColor.RED, response));
                    }
                } else {
                    sender.sendMessage(String.format("%sSorry, You don't have permissions to do this!", ChatColor.RED));
                }
            } else {
                sender.sendMessage(String.format("%sYou need to be in the game to do this!", ChatColor.RED));
            }
        } else {
            sender.sendMessage(String.format("%sYou need to be in Tun From The Beast to do this!", ChatColor.RED));
        }

        return true;
    }
}