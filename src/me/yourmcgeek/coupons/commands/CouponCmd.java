package me.yourmcgeek.coupons.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by YourMCGeek on 11/26/2016.
 */
public class CouponCmd implements CommandExecutor {
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!(sender instanceof Player)){
    		sender.sendMessage("Only players are permitted to use this command");
    		return true;
    	}
    	
		Player player = (Player) sender;
		
    	if (args.length >= 1){
    		if (args[0].equalsIgnoreCase("create")){
    			// TODO: Command logic	
    			
    			if (args[1].isEmpty()) {
    				sender.sendMessage("Unkown command argument, " + args[1] + " please try again.");
    			}
    			
    			if (args[1].matches("\\d*")&& args[1].length() >2) {
    				
    			}
    		}
    		
    		else if (args[0].equalsIgnoreCase("delete")){ // Not sure if this is going to exist or not
    			
    		}
    		
    		else{
    			player.sendMessage("Unknown command argument, " + args[0]);
    		}
    	}
    	
        return true;
    }
}