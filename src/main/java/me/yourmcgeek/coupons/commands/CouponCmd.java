package me.yourmcgeek.coupons.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
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
import me.yourmcgeek.coupons.api.PlayerRedeemCouponEvent;
import me.yourmcgeek.coupons.coupon.Coupon;
import me.yourmcgeek.coupons.coupon.CouponRegistry;
import me.yourmcgeek.coupons.utils.ConfirmationRunnable;
import me.yourmcgeek.coupons.utils.locale.Locale;

/**
 * Created by YourMCGeek on 11/26/2016.
 */
public class CouponCmd implements CommandExecutor {
	
	private final Map<UUID, ConfirmationRunnable> confirmationQueue = new HashMap<>();
	
	private final CouponCodes plugin;
	private final CouponRegistry couponRegistry;

	public CouponCmd(CouponCodes plugin) {
		this.plugin = plugin;
		this.couponRegistry = plugin.getCouponRegistry();
	}

	/*
	 * Command structure /coupon - create <code> - <material[:data][|amount]> - delete <code>
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Locale locale = plugin.getLocale();

		if (args.length == 0) {
			this.displayHelp(sender);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("create")) {
			if (!sender.hasPermission("coupons.create")) {
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons create"));
				return true;
			}
			
			if (args.length < 2) {
				sender.sendMessage(locale.getMessage("command.coupon.create.missing.code"));
				return true;
			}
			
			String code = args[1];
			
			// Check if the coupon exists before creating a new one
			if (this.couponRegistry.couponExists(code)) {
				sender.sendMessage(locale.getMessage("command.coupon.create.alreadyexists").replace("%code%", code));
				return true;
			}
			
			if (args.length < 3) {
				sender.sendMessage(locale.getMessage("command.coupon.create.missing.format"));
				return true;
			}
			
			Matcher matcher = CouponCodes.ITEM_PATTERN.matcher(args[2]);
			if (!matcher.find()) {
				sender.sendMessage(locale.getMessage("command.coupon.create.invalid.format"));
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
					sender.sendMessage(locale.getMessage("command.coupon.create.invalid.material").replace("%material%", materialString));
					continue;
				}
				
				if (material == Material.AIR) {
					sender.sendMessage(locale.getMessage("command.coupon.create.invalid.material.air"));
					continue;
				}
				
				ItemStack item = new ItemStack(material, itemCount, itemData);
				coupon.addRewards(item);
			}
			
			this.couponRegistry.registerCoupon(coupon);
			sender.sendMessage(locale.getMessage("command.coupon.create.success").replace("%code%", code));
		}
		
		else if (args[0].equalsIgnoreCase("delete")) {
			if (!sender.hasPermission("coupons.delete")) {
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons delete"));
				return true;
			}
			
			if (args.length < 2) {
				sender.sendMessage(locale.getMessage("command.coupon.delete.missing.code"));
				return true;
			}
			
			String code = args[1];
			if (!this.couponRegistry.couponExists(code)) {
				sender.sendMessage(locale.getMessage("command.general.invalidcoupon").replace("%code%", code));
				return true;
			}
			
			couponRegistry.deleteCoupon(code);
			sender.sendMessage(locale.getMessage("command.coupon.delete.success").replace("%code%", code));
		}
		
		else if (args[0].equalsIgnoreCase("redeem")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(locale.getMessage("command.coupon.redeem.playersonly"));
				return true;
			}
			
			if (!sender.hasPermission("coupon.redeem")) {
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons redeem"));
				return true;
			}

			if (args.length < 2) {
				sender.sendMessage(locale.getMessage("command.coupon.redeem.missing.code"));
				return true;
			}
			
			String code = args[1];
			if (!this.couponRegistry.couponExists(code)) {
				sender.sendMessage(locale.getMessage("command.general.invalidcoupon").replace("%code%", code));
				return true;
			}
			
			Player player = (Player) sender;
			Coupon coupon = this.couponRegistry.getCoupon(code);
			
			if (coupon.hasRedeemed(player)) {
				sender.sendMessage(locale.getMessage("command.coupon.redeem.alreadyredeemed"));
				return true;
			}
			
			if (!coupon.isRedeemable()) {
				sender.sendMessage(locale.getMessage("command.coupon.redeem.notredeemable"));
				return true;
			}
			
			// Call PlayerRedeemCouponEvent
			PlayerRedeemCouponEvent prce = new PlayerRedeemCouponEvent(player, coupon);
			Bukkit.getPluginManager().callEvent(prce);
			if (prce.isCancelled()) return true;
			
			// Add items to inventory & fire redemption action
			Inventory inventory = player.getInventory();
			coupon.getRewards().forEach(inventory::addItem);
			coupon.redeem(player);
			coupon.getRedeemAction().accept(player);
			
			sender.sendMessage(locale.getMessage("command.coupon.redeem.success").replace("%code%", code));
		}
		
		else if (args[0].equalsIgnoreCase("redeemtoggle")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(locale.getMessage("command.coupon.redeemtoggle.playersonly"));
				return true;
			}
			
			if (!sender.hasPermission("coupon.redeemtoggle")) {
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons redeemtoggle"));
				return true;
			}
			
			if (args.length < 2) {
				sender.sendMessage(locale.getMessage("command.coupon.redeemtoggle.missing.code"));
				return true;
			}
			
			String code = args[1];
			if (!this.couponRegistry.couponExists(code)) {
				sender.sendMessage(locale.getMessage("command.general.invalidcoupon").replace("%code%", code));
				return true;
			}
			
			Player player = (Player) sender;
			Coupon coupon = this.couponRegistry.getCoupon(code);
			boolean newRedeemableState = !coupon.isRedeemable();

			player.sendMessage(locale.getMessage("command.coupon.confirm.waiting").replace("%code%", code));
			
			ConfirmationRunnable runnable = new ConfirmationRunnable(plugin, 10, player, coupon, (p, c) -> {
				coupon.setRedeemable(newRedeemableState);
				p.sendMessage(locale.getMessage(newRedeemableState 
						? "command.coupon.redeemtoggle.success.enable" 
						: "command.coupon.redeemtoggle.success.disable"
					).replace("%code%", code));
			});
			this.confirmationQueue.put(player.getUniqueId(), runnable);
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
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons list"));
				return true;
			}
			
			List<String> codes = couponRegistry.getCoupons().stream()
					.map(Coupon::getCode)
					.collect(Collectors.toList());
			sender.sendMessage(locale.getMessage("command.coupon.list.listing").replace("%codes%", String.join(", ", codes)));
		}
		
		else if (args[0].equalsIgnoreCase("book")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(locale.getMessage("command.coupons.book.playersonly"));
				return true;
			}

			if (!sender.hasPermission("coupons.book")) {
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons book"));
				return true;
			}
			
			Player player = (Player) sender;
			player.getInventory().addItem(this.plugin.getInfoBook());
			sender.sendMessage(locale.getMessage("command.coupons.book.success"));
		}
		
		else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("coupons.reload")) {
				sender.sendMessage(locale.getMessage("command.general.noperms").replace("%command%", "/coupons reload"));
				return true;
			}
			
			for (Locale localeTemp : Locale.getLocales()) {
				if (!localeTemp.reloadMessages()) {
					// Obviously, if you can't reload the locale, there's no point in making a customizable message
					sender.sendMessage(ChatColor.RED + "Could not reload message for locale " + locale.getName());
				}
			}
			
			sender.sendMessage(locale.getMessage("command.coupon.reload.success"));
		}
		
		else if (args[0].equalsIgnoreCase("confirm")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(locale.getMessage("command.coupon.confirm.playersonly"));
				return true;
			}
			
			Player player = (Player) sender;
			ConfirmationRunnable confirmation = confirmationQueue.get(player.getUniqueId());
			
			if (confirmation == null) {
				player.sendMessage(locale.getMessage("command.coupon.confirm.notwaiting"));
				return true;
			}
			
			confirmation.confirm();
			this.confirmationQueue.remove(player.getUniqueId());
		}
		
		else {
			this.displayHelp(sender, args[0]);
		}
		
		return true;
	}
	
	private void displayHelp(CommandSender sender, String unknownArgument) {
		if (unknownArgument != null)
			sender.sendMessage(ChatColor.RED + "Unknown command argument: " + ChatColor.DARK_RED + unknownArgument);
		sender.sendMessage(ChatColor.RED + "/coupon <create|delete|redeem|redeemtoggle|help|list|book|reload>");
	}
	
	private void displayHelp(CommandSender sender) {
		this.displayHelp(sender, null);
	}
	
}