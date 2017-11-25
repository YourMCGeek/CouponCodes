package me.yourmcgeek.coupons.commands;

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

import me.yourmcgeek.coupons.CouponCodes;
import me.yourmcgeek.coupons.coupon.Coupon;
import me.yourmcgeek.coupons.coupon.CouponRegistry;
import me.yourmcgeek.coupons.utils.locale.Locale;

/**
 * Created by YourMCGeek on 11/26/2016.
 */
public class CouponCmd implements CommandExecutor {
	
	private static final Pattern ITEM_PATTERN = Pattern.compile("(\\w+)(?:(?:\\:{1})(\\d+)){0,1}(?:(?:\\;{1})(\\d+)){0,1}");
	
	private final CouponCodes plugin;
	private final CouponRegistry couponRegistry;
	
	public CouponCmd(CouponCodes plugin) {
		this.plugin = plugin;
		this.couponRegistry = plugin.getCouponRegistry();
	}
	
	/*
	 * Command structure /coupon - create <code> - <material[:data][|amount]> - delete <code>
	 */
	
	/*
	 * Current Commands: "create", "list", "delete", "help", "redeem"
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			this.displayHelp(sender);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("create")) {
			if (!sender.hasPermission("coupons.create")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + "/coupons create " + ChatColor.RED + "Please try again later.");
				return true;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Please specify a code for this coupon");
				return true;
			}
			
			String code = args[1];
			
			// Check if the coupon exists before creating a new one
			if (this.couponRegistry.couponExists(code)) {
				sender.sendMessage(ChatColor.RED + "A coupon with the code " + ChatColor.YELLOW + code + ChatColor.RED + " already exists");
				return true;
			}
			
			if (args.length < 3) {
				sender.sendMessage(ChatColor.RED + "Please provide a valid material format. " + ChatColor.DARK_RED + "<material[:data][|amount]>");
				return true;
			}
			
			Matcher matcher = ITEM_PATTERN.matcher(args[2]);
			if (!matcher.find()) {
				sender.sendMessage(ChatColor.RED + "Invalid material format provided. Expected: " + ChatColor.DARK_RED + "<material[:data][|amount]>");
				return true;
			}
			
			Coupon coupon = new Coupon(code);
			matcher.reset();
			while (matcher.find()) {
				String materialString = matcher.group(1).toUpperCase();
				String itemDataString = matcher.group(2);
				String itemCountString = matcher.group(3);
				
				Material material = NumberUtils.isNumber(materialString)
						? Material.getMaterial(Integer.valueOf(materialString))
						: Material.getMaterial(materialString);
				byte itemData = NumberUtils.toByte(itemDataString);
				int itemCount = NumberUtils.toInt(itemCountString, 1);
				
				if (material == null) {
					sender.sendMessage(ChatColor.RED + "Unknown material value, \"" + materialString + "\". Ignoring");
					continue;
				}
				
				if (material == Material.AIR) {
					sender.sendMessage(ChatColor.RED + "You cannot create a material with the value of \"" + materialString + "\". Ignorning");
					continue;
				}
				
				ItemStack item = new ItemStack(material, itemCount, itemData);
				coupon.addRewards(item);
			}
			
			this.couponRegistry.registerCoupon(coupon);
			sender.sendMessage(ChatColor.GREEN + "Coupon successfully created! Coupon Code: " + ChatColor.YELLOW + code);
		}
		
		else if (args[0].equalsIgnoreCase("delete")) {
			if (!sender.hasPermission("coupons.delete")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + "/coupons delete " + ChatColor.RED + "Please try again later.");
				return true;
			}
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Please specify a coupon code to delete");
				return true;
			}
			
			String code = args[1];
			if (!this.couponRegistry.couponExists(code)) {
				sender.sendMessage(ChatColor.DARK_RED + "ERROR: The following code, " + ChatColor.YELLOW + code + ChatColor.DARK_RED + ", does not exsist.");
				sender.sendMessage(ChatColor.DARK_RED + "Please try again with a valid coupon code.");
				return true;
			}
			
			couponRegistry.deleteCoupon(code);
			sender.sendMessage(ChatColor.GREEN + "Coupon code " + ChatColor.YELLOW + code + ChatColor.GREEN + " successfully deleted");
		}
		
		else if (args[0].equalsIgnoreCase("redeem")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can redeem codes, as items will be received");
				return true;
			}
			
			if (!sender.hasPermission("coupon.redeem")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + "/coupons redeem " + ChatColor.RED + "Please try again later.");
				return true;
			}
			
			Player player = (Player) sender;
			
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "Please specify a coupon code to redeem");
				return true;
			}
			
			String code = args[1];
			if (!this.couponRegistry.couponExists(code)) {
				sender.sendMessage(ChatColor.DARK_RED + "ERROR: The following code, " + ChatColor.YELLOW + code + ChatColor.DARK_RED + ", does not exsist.");
				sender.sendMessage(ChatColor.DARK_RED + "Please try again with a valid coupon code.");
				return true;
			}
			
			Coupon coupon = this.couponRegistry.getCoupon(code);
			if (coupon.hasRedeemed(player)) {
				sender.sendMessage(ChatColor.DARK_RED + "You have already redeemed this reward! You cannot redeem it again!");
				return true;
			}
			
			Inventory inventory = player.getInventory();
			coupon.getRewards().forEach(i -> inventory.addItem(i));
			coupon.redeem(player);
			
			sender.sendMessage(ChatColor.GREEN + "You have claimed the coupon code " + ChatColor.YELLOW + code);
		}
		
		else if (args[0].equalsIgnoreCase("help")) {
			ChatColor green = ChatColor.GREEN;
			sender.sendMessage(green + "Welcome to CouponCodes Help Message!");
			sender.sendMessage("                   ");
			sender.sendMessage(green + "Use \"/coupon create <code> <material>[:data]|amount\" to create a coupon.");
			sender.sendMessage(green + "Use \"/coupon delete <code>\" to delete a coupon.");
			sender.sendMessage(green + "Use \"/coupon redeem <code>\" to redeem a coupon.");
			sender.sendMessage(green + "Use \"/coupon book\" to get your nifty little guide.");
			sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "WARNING: MAKE SURE TO HAVE OPEN SPACE IN YOUR INVENTORY UPON REDEMPTION!");
		}
		
		else if (args[0].equalsIgnoreCase("list")) {
			if (!sender.hasPermission("coupons.list")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + "/coupons list " + ChatColor.RED + "Please try again later.");
				return true;
			}
			
			List<String> codes = couponRegistry.getCoupons().stream()
					.map(Coupon::getCode)
					.collect(Collectors.toList());
			sender.sendMessage(ChatColor.DARK_GREEN + "Current coupons are:");
			sender.sendMessage(ChatColor.GREEN + String.join(", ", codes));
		}
		
		else if (args[0].equalsIgnoreCase("book")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can run this command, as items will be received");
				return true;
			}
			
			Player player = (Player) sender;
			
			if (!player.hasPermission("coupons.book")) {
				player.sendMessage(ChatColor.RED + "You do not have permission for the usage to use " + ChatColor.YELLOW + "/coupons book " + ChatColor.RED + "Please try again later.");
				return true;
			}
			
			player.getInventory().addItem(this.plugin.getInfoBook());
		}
		
		else if (args[0].equalsIgnoreCase("reload")) {
			for (Locale locale : Locale.getLocales()) {
				if (!locale.reloadMessages()) {
					sender.sendMessage(ChatColor.RED + "Could not reload message for locale " + locale.getName());
				}
			}
			
			sender.sendMessage(ChatColor.GREEN + "Configuration file successfully reloaded");
		}
		
		else {
			this.displayHelp(sender, args[0]);
		}
		
		return true;
	}
	
	private void displayHelp(CommandSender sender, String unknownArgument) {
		if (unknownArgument != null)
			sender.sendMessage(ChatColor.RED + "Unknown command argument: " + ChatColor.DARK_RED + unknownArgument);
		sender.sendMessage(ChatColor.RED + "/coupon <create|delete|redeem|help|list|book>");
	}
	
	private void displayHelp(CommandSender sender) {
		this.displayHelp(sender, null);
	}
	
}