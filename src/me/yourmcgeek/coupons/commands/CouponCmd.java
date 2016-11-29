package me.yourmcgeek.coupons.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
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
    				
    				// Check if the coupon exists before creating a new one
    				if (this.couponRegistry.couponExists(code)) {
    					player.sendMessage(ChatColor.RED + "A coupon with the code " + ChatColor.YELLOW + code + ChatColor.RED + " already exists");
    					return true;
    				}
    				
    				if (args.length >= 3) {
    					Matcher matcher = ITEM_PATTERN.matcher(args[2]);
    					if (!matcher.find()){
    						player.sendMessage(ChatColor.RED + "Invalid material format provided. Expected: " + ChatColor.DARK_RED + "<material[:data][|amount]>");
    						return true;
    					}
    					
    					Coupon coupon = new Coupon(code);
    					matcher.reset();
    					while (matcher.find()){
    						String materialString = matcher.group(1);
    						String itemDataString = matcher.group(2);
    						String itemCountString = matcher.group(3);
    						
    						Material material = Material.valueOf(materialString);
    						byte itemData = NumberUtils.toByte(itemDataString);
    						int itemCount = NumberUtils.toInt(itemCountString, 1);
    						
    						if (material == null){
    							player.sendMessage(ChatColor.RED + "Unknown material value, \"" + materialString + "\". Ignoring");
    							continue;
    						}
    						
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
    		
    		else if (args[0].equalsIgnoreCase("help")) {
    			ChatColor green = ChatColor.GREEN;
    			sender.sendMessage(green + "Welcome to CouponCodes Help Message!");
    			sender.sendMessage("                   ");
    			sender.sendMessage(green + "Use /coupon create {Code} {Number of items} {item name} to create a coupon.");
    			sender.sendMessage(green + "Use /coupon delete {Code} to delete a coupon.");
    			sender.sendMessage(green + "Use /coupon redeem {Code} to redeem a coupon.");
    			sender.sendMessage(ChatColor.DARK_RED + "WARNING: MAKE SURE TO HAVE OPEN SPACE IN YOUR INVENTORY!");
    		}
    		
    		else{
    			player.sendMessage("Unknown command argument, " + args[0]);
    		}
    	}
    	
        return true;
    }
}