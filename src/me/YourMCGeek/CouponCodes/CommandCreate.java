package me.YourMCGeek.CouponCodes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Tucker on 11/26/2016.
 */
public class CommandCreate implements CommandExecutor {
    //coupon create
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("coupon create")) {
            if(sender instanceof Player) {
                Player player = (Player) sender;

            }
        }
        return true;
    }

}
