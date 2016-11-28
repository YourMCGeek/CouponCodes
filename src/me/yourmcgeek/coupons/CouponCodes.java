package me.yourmcgeek.coupons;

import org.bukkit.plugin.java.JavaPlugin;

import me.yourmcgeek.coupons.commands.CouponCmd;

/**
 * Created by YourMCGeek on 11/26/2016.
 */
public class CouponCodes extends JavaPlugin {

	@Override
	public void onEnable() {
		this.getLogger().info("CouponCodes is ready to provide discounts!");

		this.getCommand("coupon").setExecutor(new CouponCmd());
	}

	@Override
	public void onDisable() {
		this.getLogger().info("CouponCodes has run out of discounts!");
	}
}