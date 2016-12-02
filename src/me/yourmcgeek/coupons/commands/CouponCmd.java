package me.yourmcgeek.coupons.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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
				
				String Title = "Coupons Help Book";
				String Author = "YourMCGeek and 2008Choco";
				String displayName = "�3�lCoupons Help Book";
				String Lore = "A nice little guide to Coupons!";
				

				
				if (!sender.hasPermission("coupons.book")) {
					
					ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
					BookMeta bMeta = (BookMeta) book.getItemMeta();
					bMeta.setDisplayName(displayName);
					ArrayList<String> mLore = new ArrayList<String>();
					mLore.add(Lore);
					bMeta.setLore(mLore);
					bMeta.setAuthor(Author);
					bMeta.setTitle(Title);
					
					String [] pages = new String[17];
					
					//Page 1:
					pages[0] = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Welcome to Coupons!\n" + 
					ChatColor.BLUE + "To create a coupon, run" + ChatColor.DARK_GREEN + " /coupons create {code} <material>[:data]|amount. \n" +
							ChatColor.BLUE + "An example would be " + ChatColor.DARK_GREEN + "/coupons create CBE dirt|64";					
					
					bMeta.setPages(pages);
					book.setItemMeta(bMeta);
					((Player) sender).getInventory().addItem(book);
					
					
					
				}
			}

			else{
				sender.sendMessage(ChatColor.RED + "Unknown command argument: " + ChatColor.DARK_RED + args[0]);
				sender.sendMessage(ChatColor.RED + "/coupon <create|delete|redeem|help|list>");
			}
	/*	else {
	*		sender.sendMessage(ChatColor.RED + "/coupon <create|delete|redeem|help|list>");
	*/	
	}
		return true;
	}
}