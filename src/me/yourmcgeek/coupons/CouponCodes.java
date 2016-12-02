package me.yourmcgeek.coupons;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.yourmcgeek.coupons.commands.CouponCmd;
import me.yourmcgeek.coupons.utils.Coupon;
import me.yourmcgeek.coupons.utils.CouponRegistry;
import me.yourmcgeek.coupons.utils.general.BookUtils;
import me.yourmcgeek.coupons.utils.general.ConfigAccessor;

/**
 * Created by YourMCGeek and 2008Choco on 11/26/2016.
 */
public class CouponCodes extends JavaPlugin {
	
	static{
		ConfigurationSerialization.registerClass(Coupon.class, "Coupon");
	}
	
	private ItemStack infoBook;
	
	public ConfigAccessor couponFile;
	
	private CouponRegistry couponRegistry;
	private boolean generateDefaultCoupon = false;

	@Override
	public void onEnable() {
		this.getLogger().info("CouponCodes is ready to provide discounts!");
		if (!this.getDataFolder().exists()) 
			this.generateDefaultCoupon = true;
		
		// Field initialization
		this.couponFile = new ConfigAccessor(this, "coupons.yml");
		this.couponFile.loadConfig();
		
		this.couponRegistry = new CouponRegistry();
		this.infoBook = BookUtils.generateBook(this);
		
		// Command registration
		this.getCommand("coupon").setExecutor(new CouponCmd(this));
		
		// Load all saved coupons
		for (String couponCode : couponFile.getConfig().getKeys(false)){
			Coupon coupon = (Coupon) this.couponFile.getConfig().get(couponCode);
			if (coupon == null){
				this.getLogger().warning("Could not load coupon with UUID " + couponCode + ". Ignoring");
				continue;
			}
			
			this.couponRegistry.registerCoupon(coupon);
		}
		
		// Generate default coupon
		if (generateDefaultCoupon){
			this.couponRegistry.createCoupon("default", new ItemStack(Material.DIAMOND));
			this.getLogger().info("Generated default coupon");
			this.getLogger().info("Code: \"default\"");
		}
	}

	@Override
	public void onDisable() {
		this.getLogger().info("CouponCodes has run out of discounts!");
		
		for (Coupon coupon : this.couponRegistry.getCoupons())
			// Remember that "Coupon" is ConfigurationSerializable, so it should be serialized properly :D
			this.couponFile.getConfig().set(coupon.getCode(), coupon);
		this.couponFile.saveConfig();
		
		this.couponRegistry.clearCouponData();
	}
	
	/** Get an instance of the coupon registry
	 * @return the coupon registry
	 */
	public CouponRegistry getCouponRegistry() {
		return couponRegistry;
	}
	
	public ItemStack getInfoBook() {
		return infoBook;
	}
}