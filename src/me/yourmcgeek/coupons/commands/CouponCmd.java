package me.yourmcgeek.coupons.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
	
	/* Current Commands:
	 * 
	 * Create
	 * List
	 * Delete
	 * Help
	 * Redeem
	 */

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length >= 1){
			if (args[0].equalsIgnoreCase("create")){
				if (!sender.hasPermission("coupons.create")){
					sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + 
							"/coupons create " + ChatColor.RED + "Please try again later.");
					return true;
				}
				
				if (args.length >= 2) {
					String code = args[1];

					// Check if the coupon exists before creating a new one
					if (this.couponRegistry.couponExists(code)) {
						sender.sendMessage(ChatColor.RED + "A coupon with the code " + ChatColor.YELLOW + code + ChatColor.RED + " already exists");
						return true;
					}

					if (args.length >= 3) {
						Matcher matcher = ITEM_PATTERN.matcher(args[2]);
						if (!matcher.find()){
							sender.sendMessage(ChatColor.RED + "Invalid material format provided. Expected: " + ChatColor.DARK_RED + "<material[:data][|amount]>");
							return true;
						}

						Coupon coupon = new Coupon(code);
						matcher.reset();
						while (matcher.find()){
							String materialString = matcher.group(1).toUpperCase();
							String itemDataString = matcher.group(2);
							String itemCountString = matcher.group(3);

							Material material = Material.getMaterial(materialString);
							byte itemData = NumberUtils.toByte(itemDataString);
							int itemCount = NumberUtils.toInt(itemCountString, 1);

							if (material == null){
								sender.sendMessage(ChatColor.RED + "Unknown material value, \"" + materialString + "\". Ignoring");
								continue;
							}

							ItemStack item = new ItemStack(material, itemCount, itemData);
							coupon.addRewards(item);
						}

						this.couponRegistry.registerCoupon(coupon);
						sender.sendMessage(ChatColor.GREEN + "Coupon successfully created! Coupon Code: " + ChatColor.YELLOW + code);
					}

					else {
						sender.sendMessage(ChatColor.RED + "Please provide a valid material format. " + ChatColor.DARK_RED + "<material[:data][|amount]>");
					}
				}
				else{
					sender.sendMessage(ChatColor.RED + "Please specify a code for this coupon");
				}
			}
			
			else if (args[0].equalsIgnoreCase("delete")){
				if (!sender.hasPermission("coupons.delete")){
					sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + 
							"/coupons delete " + ChatColor.RED + "Please try again later.");
					return true;
				}
				
				if (args.length >= 2) {
					String code = args[1];
					if (!this.couponRegistry.couponExists(code)){
						sender.sendMessage(ChatColor.DARK_RED + "ERROR: The following code, " + ChatColor.YELLOW + code + ChatColor.DARK_RED + ", does not exsist.");
						sender.sendMessage(ChatColor.DARK_RED + "Please try again with a valid coupon code.");
						return true;
					}

					couponRegistry.deleteCoupon(code);
					sender.sendMessage(ChatColor.GREEN + "Coupon code " + ChatColor.YELLOW + code + ChatColor.GREEN + " successfully deleted");
				}
				else{
					sender.sendMessage(ChatColor.RED + "Please specify a coupon code to delete");
				}
			}
			
			else if (args[0].equalsIgnoreCase("redeem")){
				if (!(sender instanceof Player)){
					sender.sendMessage("Only players can redeem codes, as items will be received");
					return true;
				}
				
				if (!sender.hasPermission("coupon.redeem")){
					sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + 
							"/coupons redeem " + ChatColor.RED + "Please try again later.");
					return true;
				}
				
				Player player = (Player) sender;
				
				if (args.length >= 2) {
					String code = args[1];
					if (!this.couponRegistry.couponExists(code)){
						sender.sendMessage(ChatColor.DARK_RED + "ERROR: The following code, " + ChatColor.YELLOW + code + ChatColor.DARK_RED + ", does not exsist.");
						sender.sendMessage(ChatColor.DARK_RED + "Please try again with a valid coupon code.");
						return true;
					}
					
					Coupon coupon = this.couponRegistry.getCoupon(code);
					if (coupon.hasRedeemed(player)){
						sender.sendMessage(ChatColor.DARK_RED + "You have already redeemed this reward! You cannot redeem it again!");
						return true;
					}
					
					Inventory inventory = player.getInventory();
					coupon.getRewards().forEach(i -> inventory.addItem(i));
					coupon.setRedeemed(player);
					
					sender.sendMessage(ChatColor.GREEN + "You have claimed the coupon code " + ChatColor.YELLOW + code);
				}
				else{
					sender.sendMessage(ChatColor.RED + "Please specify a coupon code to redeem");
				}
			}

			else if (args[0].equalsIgnoreCase("help")) {
				ChatColor green = ChatColor.GREEN;
				sender.sendMessage(green + "Welcome to CouponCodes Help Message!");
				sender.sendMessage("                   ");
				sender.sendMessage(green + "Use /coupon create {Code} {Number of items} {item name} to create a coupon.");
				sender.sendMessage(green + "Use /coupon delete {Code} to delete a coupon.");
				sender.sendMessage(green + "Use /coupon redeem {Code} to redeem a coupon.");
				sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "WARNING: MAKE SURE TO HAVE OPEN SPACE IN YOUR INVENTORY UPON REDEMTION!");
			}

			else if (args[0].equalsIgnoreCase("list")) {
				if (!sender.hasPermission("coupons.list")){
					sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + 
							"/coupons list " + ChatColor.RED + "Please try again later.");
					return true;
				}

				List<String> codes = couponRegistry.getCoupons().stream().map(Coupon::getCode).collect(Collectors.toList());
				sender.sendMessage(ChatColor.GREEN + "Current coupons are:");
				sender.sendMessage(ChatColor.GREEN + String.join(", ", codes));
			}
			
			else if (args[0].equalsIgnoreCase("book")) {
				if (!sender.hasPermission("coupons.book")) {
					Bukkit.dispatchCommand(sender, "/give sender written_book 1 0 {pages:["[\"\",{\"text\":\"Welcome to Coupons!\",\"bold\":true,\"underlined\":true,\"color\":\"dark_green\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"To create a coupon, do \",\"color\":\"dark_aqua\"},{\"text\":\"/coupons create {code} [:data]|amount\",\"color\":\"dark_green\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"An example would be \",\"color\":\"dark_aqua\"},{\"text\":\"/coupons create CBE dirt|64\",\"color\":\"dark_green\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"To create that coupon,\nclick \",\"color\":\"dark_aqua\"},{\"text\":\"here\",\"color\":\"dark_aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/coupon create CBE dirt|64\"}},{\"text\":\" or type it.\",\"color\":\"dark_aqua\"},{\"text\":\"\n\n \",\"color\":\"reset\"}]","[\"\",{\"text\":\"To view the current list of coupons, you can run the command.\",\"color\":\"dark_blue\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"/coupons list\",\"color\":\"dark_aqua\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"Click \",\"color\":\"dark_blue\"},{\"text\":\"here\",\"color\":\"dark_blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/coupons list\"}},{\"text\":\" or type it in chat to run the command and see our freshly created CBE coupon!\",\"color\":\"dark_blue\"},{\"text\":\"\n\n \",\"color\":\"reset\"}]","[\"\",{\"text\":\"To redeem a coupon, run the command \",\"color\":\"dark_purple\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"/coupon redeem {code}\",\"color\":\"light_purple\"},{\"text\":\"\n\n\n\",\"color\":\"reset\"},{\"text\":\"Lets redeem our coupon by doing \",\"color\":\"dark_purple\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"/coupon redeem CBE\",\"color\":\"light_purple\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"Click \",\"color\":\"dark_purple\"},{\"text\":\"here\",\"color\":\"dark_purple\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/coupon redeem CBE\"}},{\"text\":\" or type in chat!\",\"color\":\"dark_purple\"}]","[\"\",{\"text\":\"Since we no longer need our coupon, lets delete it. To delete a coupon do,\",\"color\":\"dark_blue\"},{\"text\":\" \",\"color\":\"reset\"},{\"text\":\"/coupons delete {code}\",\"color\":\"blue\"},{\"text\":\".\n\n\",\"color\":\"reset\"},{\"text\":\"To delete our coupon, lets run \",\"color\":\"dark_blue\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"/coupons delete CBE\",\"color\":\"blue\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"Click here or type it!!\",\"color\":\"dark_blue\"},{\"text\":\"\n \",\"color\":\"reset\"}]","[\"\",{\"text\":\"Other commands include\",\"color\":\"dark_red\"},{\"text\":\"\n\n\",\"color\":\"reset\"},{\"text\":\"/coupons help\n\n/coupons book\",\"color\":\"red\"},{\"text\":\"\n\n\n \",\"color\":\"reset\"}]","[\"\",{\"text\":\"\n\"},{\"text\":\"asdfasdfasdfasdfasdfa\",\"obfuscated\":true,\"color\":\"gold\"},{\"text\":\"\n\",\"color\":\"reset\"},{\"text\":\"    Happy Couponing\n   ~ YourMCGeek and         2008Choco!! <3\",\"color\":\"dark_gray\"},{\"text\":\"\n\",\"color\":\"reset\"},{\"text\":\"asdfasdfasdfasdfasdfa\",\"obfuscated\":true,\"color\":\"gold\"},{\"text\":\"\n\n \",\"color\":\"reset\"}]"],title:"Coupons Tutorial",author:"YourMCGeek and 2008Choco",display:{Lore:["A nice little guide to Coupons!"]}}");
				}
			}

			else{
				sender.sendMessage(ChatColor.RED + "Unknown command argument: " + ChatColor.DARK_RED + args[0]);
				sender.sendMessage(ChatColor.RED + "/coupon <create|delete|redeem|help|list>");
			}
	/*	else {
	*		sender.sendMessage(ChatColor.RED + "/coupon <create|delete|redeem|help|list>");
	*/	
		return true;
	}
}