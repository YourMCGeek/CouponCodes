package me.yourmcgeek.coupons;

import org.bukkit.plugin.java.JavaPlugin;

import me.yourmcgeek.coupons.commands.CouponCmd;
import me.yourmcgeek.coupons.utils.CouponRegistry;

/**
 * Created by YourMCGeek on 11/26/2016.
 */
public class CouponCodes extends JavaPlugin {
	
	private CouponRegistry couponRegistry;

	@Override
	public void onEnable() {
		this.getLogger().info("CouponCodes is ready to provide discounts!");
		
		// Field initialization
		this.couponRegistry = new CouponRegistry();
		
		// Event registration
		
		// Command registration
		this.getCommand("coupon").setExecutor(new CouponCmd(this));
	}

	@Override
	public void onDisable() {
		this.getLogger().info("CouponCodes has run out of discounts!");
	}
	
	/** Get an instance of the coupon registry
	 * @return the coupon registry
	 */
	public CouponRegistry getCouponRegistry() {
		return couponRegistry;
	}
}