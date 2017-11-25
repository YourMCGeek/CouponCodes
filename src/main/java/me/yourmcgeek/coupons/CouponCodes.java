package me.yourmcgeek.coupons;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.yourmcgeek.coupons.commands.CouponCmd;
import me.yourmcgeek.coupons.coupon.Coupon;
import me.yourmcgeek.coupons.coupon.CouponRegistry;
import me.yourmcgeek.coupons.utils.BookUtils;
import me.yourmcgeek.coupons.utils.ConfigAccessor;
import me.yourmcgeek.coupons.utils.locale.Locale;

/**
 * Created by YourMCGeek and 2008Choco on 11/26/2016.
 */
public class CouponCodes extends JavaPlugin {

	static {
		ConfigurationSerialization.registerClass(Coupon.class, "Coupon");
	}

	private ItemStack infoBook;

	private Locale locale;
	public ConfigAccessor couponFile;

	private CouponRegistry couponRegistry;

	@Override
	public void onEnable() {
		boolean generateDefaultCoupon = getDataFolder().exists();
		this.saveDefaultConfig();

		// Generate localizations
		Locale.init(this);
		Locale.saveDefaultLocale("en_US");
		Locale.saveDefaultLocale("fr_CA");
		locale = Locale.getLocale(this.getConfig().getString("Locale", "en_US"));

		// Field initialization
		this.couponFile = new ConfigAccessor(this, "coupons.yml");
		this.couponFile.loadConfig();

		this.couponRegistry = new CouponRegistry();
		this.infoBook = BookUtils.generateBook(this);

		// Command registration
		this.getCommand("coupon").setExecutor(new CouponCmd(this));

		// Load all saved coupons
		for (String couponCode : couponFile.getConfig().getKeys(false)) {
			Coupon coupon = (Coupon) this.couponFile.getConfig().get(couponCode);

			if (coupon == null) {
				this.getLogger().warning("Could not load coupon with UUID " + couponCode + ". Ignoring");
				continue;
			}

			this.couponRegistry.registerCoupon(coupon);
		}

		// Generate default coupon
		if (generateDefaultCoupon) {
			this.couponRegistry.createCoupon("default", new ItemStack(Material.DIAMOND));
			this.getLogger().info("Generated default coupon");
			this.getLogger().info("Code: \"default\"");
		}
	}

	@Override
	public void onDisable() {
		for (Coupon coupon : this.couponRegistry.getCoupons())
			this.couponFile.getConfig().set(coupon.getCode(), coupon);
		this.couponFile.saveConfig();

		this.couponRegistry.clearCouponData();
	}

	/**
	 * Get an instance of the coupon registry
	 *
	 * @return the coupon registry
	 */
	public CouponRegistry getCouponRegistry() {
		return couponRegistry;
	}

	/**
	 * Get the informational book ItemStack for CouponCodes
	 *
	 * @return informational book
	 */
	public ItemStack getInfoBook() {
		return infoBook;
	}

	/**
	 * Get the currently active locale
	 *
	 * @return active locale
	 */
	public Locale getLocale() {
		return locale;
	}
}