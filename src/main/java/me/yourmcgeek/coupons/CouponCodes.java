package me.yourmcgeek.coupons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.yourmcgeek.coupons.commands.CouponCmd;
import me.yourmcgeek.coupons.coupon.Coupon;
import me.yourmcgeek.coupons.coupon.CouponRegistry;
import me.yourmcgeek.coupons.utils.BookUtils;
import me.yourmcgeek.coupons.utils.locale.Locale;

/**
 * Created by YourMCGeek and 2008Choco on 11/26/2016.
 */
public class CouponCodes extends JavaPlugin {
	
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Coupon.class, new Coupon.CouponSerializer())
			.registerTypeAdapter(Coupon.class, new Coupon.CouponDeserializer())
			.setPrettyPrinting().create();

	private ItemStack infoBook;

	private Locale locale;
	private File couponFile;

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
		this.couponFile = new File("coupons.json");

		this.couponRegistry = new CouponRegistry();
		this.infoBook = BookUtils.generateBook(this);

		// Command registration
		this.getCommand("coupon").setExecutor(new CouponCmd(this));

		// Load all saved coupons
		try (BufferedReader reader = new BufferedReader(new FileReader(couponFile))) {
			for (JsonElement couponData : GSON.fromJson(reader, JsonArray.class)) {
				Coupon coupon = GSON.fromJson(couponData, Coupon.class);
				
				if (coupon == null) {
					this.getLogger().warning("Could not load coupon with data \"" + couponData + "\". Ignoring");
					continue;
				}

				this.couponRegistry.registerCoupon(coupon);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
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
		if (couponFile.exists() && couponRegistry.getCoupons().size() > 0) {
			// Write coupons to file in the form of a JsonArray
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(couponFile))) {
				JsonArray coupons = this.couponRegistry.getCoupons().stream()
						.map(GSON::toJsonTree)
						.collect(JsonArray::new, JsonArray::add, JsonArray::add);
				writer.write(GSON.toJson(coupons));
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			this.couponRegistry.clearCouponData();
		}
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