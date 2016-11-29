package me.yourmcgeek.coupons;

import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import me.yourmcgeek.coupons.commands.CouponCmd;
import me.yourmcgeek.coupons.utils.Coupon;
import me.yourmcgeek.coupons.utils.CouponRegistry;
import me.yourmcgeek.coupons.utils.general.ConfigAccessor;

/**
 * Created by YourMCGeek and 2008Choco on 11/26/2016.
 */
public class CouponCodes extends JavaPlugin {
	
	static{
		ConfigurationSerialization.registerClass(Coupon.class, "Coupon");
	}
	
	public ConfigAccessor couponFile;
	
	private CouponRegistry couponRegistry;

	@Override
	public void onEnable() {
		this.getLogger().info("CouponCodes is ready to provide discounts!");
		
		// Field initialization
		this.couponFile = new ConfigAccessor(this, "coupons.yml");
		this.couponFile.loadConfig();
		
		this.couponRegistry = new CouponRegistry();
		
		// Event registration
		
		// Command registration
		this.getCommand("coupon").setExecutor(new CouponCmd(this));
		
		// Load all saved coupons
		for (String couponUUID : couponFile.getConfig().getKeys(false)){
			Coupon coupon = (Coupon) this.couponFile.getConfig().get(couponUUID);
			if (coupon == null){
				this.getLogger().warning("Could not load coupon with UUID " + couponUUID + ". Ignoring");
				continue;
			}
			
			this.couponRegistry.registerCoupon(coupon);
		}
	}

	@Override
	public void onDisable() {
		this.getLogger().info("CouponCodes has run out of discounts!");
		
		for (Coupon coupon : this.couponRegistry.getCoupons())
			// Remember that "Coupon" is ConfigurationSerializable, so it should be serialized properly :D
			this.couponFile.getConfig().set(UUID.randomUUID().toString(), coupon);
		this.couponRegistry.clearCouponData();
	}
	
	/** Get an instance of the coupon registry
	 * @return the coupon registry
	 */
	public CouponRegistry getCouponRegistry() {
		return couponRegistry;
	}
}