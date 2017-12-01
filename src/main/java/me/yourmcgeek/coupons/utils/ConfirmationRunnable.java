package me.yourmcgeek.coupons.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.yourmcgeek.coupons.CouponCodes;
import me.yourmcgeek.coupons.coupon.Coupon;
import me.yourmcgeek.coupons.utils.locale.Locale;

/**
 * A BukkitRunnable implementation to count down from a specified time and execute
 * a confirmation action upon calling {@link #confirm()}
 * 
 * @author Parker Hawke - 2008Choco
 */
public class ConfirmationRunnable extends BukkitRunnable {
	
	private int time;
	private final int maxTime;
	
	private final Player player;
	private final Coupon coupon;
	
	private final CouponCodes plugin;
	private final Consumer<Player> confirmationAction;
	private final BiConsumer<Player, Coupon> confirmationActionWithCoupon;
	
	/**
	 * Construct a new ConfirmationRunnable where the only required confirmation parameter is a Player
	 * 
	 * @param plugin an instance of the CouponCodes plugin
	 * @param time the time limit for the confirmation
	 * @param player the player from which to request confirmation
	 * @param confirmationAction the action to perform upon confirmation
	 */
	public ConfirmationRunnable(CouponCodes plugin, int time, Player player, Consumer<Player> confirmationAction) {
		Validate.notNull(plugin, "Cannot create a runnable with a null plugin");
		Validate.isTrue(time > 0, "Time must be greater than 0");
		Validate.notNull(player, "Cannot pass a null player to the confirmation runnable");
		
		this.time = time;
		this.maxTime = time;
		this.plugin = plugin;
		this.player = player;
		this.coupon = null;
		this.confirmationAction = (confirmationAction != null) ? confirmationAction : p -> {};
		this.confirmationActionWithCoupon = null;
		
		this.runTaskTimer(plugin, 0, 20);
	}
	
	/**
	 * Construct a new ConfirmationRunnable where both a Player and a Coupon instance is required for 
	 * confirmation
	 * 
	 * @param plugin an instance of the CouponCodes plugin
	 * @param time the time limit for the confirmation
	 * @param player the player from which to request confirmation
	 * @param coupon the coupon partaking in confirmation with the player
	 * @param confirmationAction the action to perform upon confirmation
	 */
	public ConfirmationRunnable(CouponCodes plugin, int time, Player player, Coupon coupon, BiConsumer<Player, Coupon> confirmationAction) {
		Validate.notNull(plugin, "Cannot create a runnable with a null plugin");
		Validate.isTrue(time > 0, "Time must be greater than 0");
		Validate.notNull(player, "Cannot pass a null player to the confirmation runnable");
		Validate.notNull(coupon, "Cannot pass a null coupon to the confirmation runnable");
		
		this.time = time;
		this.maxTime = time;
		this.plugin = plugin;
		this.player = player;
		this.coupon = coupon;
		this.confirmationAction = null;
		this.confirmationActionWithCoupon = (confirmationAction != null) ? confirmationAction : (p, c) -> {};
		
		this.runTaskTimer(plugin, 0, 20);
	}
	
	@Override
	public void run() {
		Locale locale = plugin.getLocale();
		
		String title = getColorForTime(time).toString() + ChatColor.BOLD + locale.getMessage("command.coupon.confirm.title.title").replace("%time%", String.valueOf(time));
		String subtitle = locale.getMessage("command.coupon.confirm.title.subtitle");
		this.player.sendTitle(title, subtitle, 0, 1, 0);
		this.player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, ((float) time / (float) maxTime) * 2.0f);
		
		if (--time == 0) {
			this.player.sendMessage(locale.getMessage("command.coupon.confirm.failed"));
			this.cancel();
		}
	}
	
	/**
	 * Attempt to confirm this runnable and execute the confirmation action
	 * 
	 * @return true if successfully confirmed, false if time ran out
	 */
	public boolean confirm() {
		if (time <= 0) return false; // Impossible to redeem. Time already ran out
		
		if (confirmationActionWithCoupon != null) {
			this.confirmationActionWithCoupon.accept(player, coupon);
		}
		else {
			this.confirmationAction.accept(player);
		}
		
		this.cancel();
		return true;
	}
	
	private ChatColor getColorForTime(int time) {
		switch (time) {
			case 8:
			case 7:
				return ChatColor.YELLOW;
			case 6:
			case 5:
				return ChatColor.GOLD;
			case 4:
			case 3:
				return ChatColor.RED;
			case 2:
			case 1:
				return ChatColor.DARK_RED;
			default:
				return ChatColor.GREEN;
		}
	}
	
}