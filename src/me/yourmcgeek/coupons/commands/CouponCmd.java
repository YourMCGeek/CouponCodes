package me.yourmcgeek.coupons.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.yourmcgeek.coupons.CouponCodes;
import me.yourmcgeek.coupons.utils.Coupon;
import me.yourmcgeek.coupons.utils.CouponRegistry;

/**
 * Created by YourMCGeek on 11/26/2016.
 */
public class CouponCmd implements CommandExecutor {
	
	private static final Pattern ITEM_PATTERN = Pattern.compile("(\\w+)(?:(?:\\:{1})(\\d+)){0,1}(?:(?:\\|{1})(\\d+)){0,1}");
	
	private final CouponRegistry couponRegistry;
	public CouponCmd(CouponCodes plugin) {
		this.couponRegistry = plugin.getCouponRegistry();
	}
	
	/* Command structure
	 *     /coupon
	 *       - create <code>
	 *         - <material[:data][|amount]>
	 *       - delete <code>
	 */
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (!(sender instanceof Player)){
    		sender.sendMessage("Only players are permitted to use this command");
    		return true;
    	}
    	
		Player player = (Player) sender;
		
    	if (args.length >= 1){
    		if (args[0].equalsIgnoreCase("create")){
    			if (args.length >= 2) {
    				String code = args[1];
    				
    				if (args.length >= 3) {
    					Matcher matcher = ITEM_PATTERN.matcher(args[2]);
    					if (!matcher.find()){
    						player.sendMessage("Invalid material format provided. Expected: " + ChatColor.DARK_RED + "<material[:data][|amount]>");
    						return true;
    					}
    					
    					Coupon coupon = new Coupon(code);
    					matcher.reset();
    					while (matcher.find()){
    						String materialString = matcher.group(1);
    						String itemDataString = matcher.group(2);
    						String itemCountString = matcher.group(3);
    						
    						Material material = Material.valueOf(materialString);
    						byte itemData = 0;
    						int itemCount = 1;
    						
    						if (material == null){
    							player.sendMessage("Unknown material value, \"" + materialString + "\". Ignoring");
    							continue;
    						}
    						
    						// Data parsing
    						try{
    							itemData = Byte.parseByte(itemDataString);
    						}catch(NumberFormatException e){}
    						
    						// Item count parsing
    						try{
    							itemCount = Integer.parseInt(itemCountString);
    						}catch(NumberFormatException e){}
    						
    						ItemStack item = new ItemStack(material, itemCount, itemData);
    						coupon.addRewards(item);
    					}
    					
    					this.couponRegistry.registerCoupon(coupon);
    					player.sendMessage(ChatColor.GREEN + "Coupon successfully created! Coupon Code: " + ChatColor.YELLOW + code);
    				}
    				
    				else {
    					player.sendMessage(ChatColor.RED + "Please provide a valid material format. " + ChatColor.DARK_RED + "<material[:data][|amount]>");
    				}
    			}
    			
    			else {
    				sender.sendMessage(ChatColor.RED + "Unknown command argument, " + ChatColor.DARK_RED + args[1]);
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