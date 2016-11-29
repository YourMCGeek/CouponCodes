package me.yourmcgeek.coupons.commands;

import org.bukkit.Material;
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
    			// /coupon create CODEHERE 32 DIAMOND 
    			
    			if (args[1].isEmpty()) {
    				sender.sendMessage("§cUnkown command argument, §4" + args[1] + " please try again.");
    			}
    			
    			
    			
    			else if (args[2].matches("\\d*")&& args[1].length() > 2) {
    				if (args[3].isEmpty()) {
    					sender.sendMessage("§cPlease enter an item.");
    				}
    				else {
    					Material item = Material.getMaterial(args[3].toUpperCase());
    					if (item == null) {
    						sender.sendMessage("§cPlease provide a valid material name. §4" + args[3] + "§c is not a valid material name"
    								+ "please try again.");
    					}
    					else {
    						
    					}
    				}
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