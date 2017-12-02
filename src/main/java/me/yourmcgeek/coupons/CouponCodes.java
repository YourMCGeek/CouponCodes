package me.yourmcgeek.coupons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

	public static final Pattern ITEM_PATTERN = Pattern.compile("(\\w+)(?:(?:\\:{1})(\\d+)){0,1}(?:(?:\\;{1})(\\d+)){0,1}");
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Coupon.class, new Coupon.CouponSerializer())
			.registerTypeAdapter(Coupon.class, new Coupon.CouponDeserializer())
			.setPrettyPrinting().create();
	private static final File COUPON_FILE = new File("coupons.json");

	private Locale locale;
	private ItemStack infoBook;
	private CouponRegistry couponRegistry;

	@Override
	public void onEnable() {
		boolean generateDefaultCoupon = !COUPON_FILE.exists();
		this.saveDefaultConfig();
		
		try {
			COUPON_FILE.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Generate localizations
		Locale.init(this);
		Locale.saveDefaultLocale("en_US");
		Locale.saveDefaultLocale("fr_CA");
		locale = Locale.getLocale(this.getConfig().getString("Locale", "en_US"));

		// Field initialization
		this.couponRegistry = new CouponRegistry();
		this.infoBook = BookUtils.generateBook(this);

		// Command registration
		this.getCommand("coupon").setExecutor(new CouponCmd(this));

		// Load all saved coupons
		this.loadCouponsFromFile().forEach(couponRegistry::registerCoupon);

		// Generate default coupon
		if (generateDefaultCoupon) {
			this.couponRegistry.createCoupon("default", new ItemStack(Material.DIAMOND));
			this.getLogger().info(locale.getMessage("enable.coupongeneration.default").replace("%code%", "default"));
		}
	}

	@Override
	public void onDisable() {
		if (COUPON_FILE.exists() && couponRegistry.getCoupons().size() > 0) {
			this.saveCouponsToFile();
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
	
	private void saveCouponsToFile() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUPON_FILE))) {
			JsonArray coupons = this.couponRegistry.getCoupons().stream()
					.map(GSON::toJsonTree)
					.collect(JsonArray::new, JsonArray::add, JsonArray::add);
			writer.write(GSON.toJson(coupons));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<Coupon> loadCouponsFromFile() {
		List<Coupon> coupons = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(COUPON_FILE))) {
			JsonArray couponArrayData = GSON.fromJson(reader, JsonArray.class);
			if (couponArrayData == null) return coupons;
			
			for (JsonElement couponData : couponArrayData) {
				Coupon coupon = GSON.fromJson(couponData, Coupon.class);
				
				if (coupon == null) {
					this.getLogger().warning(locale.getMessage("enable.coupongeneration.failed").replace("%data%", couponData.toString()));
					continue;
				}
				
				coupons.add(coupon);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return coupons;
	}
	
}