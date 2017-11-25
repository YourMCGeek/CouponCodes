package me.yourmcgeek.coupons.coupon;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableSet;

/**
 * A registry to store all data regarding coupons and their current status on the server. All
 * coupons should be registered here to be managed and maintained by the plugin
 * 
 * @author Parker Hawke - 2008Choco
 */
public class CouponRegistry {
	
	private final Set<Coupon> coupons = new HashSet<>();
	
	/**
	 * Register a coupon to the registry
	 * 
	 * @param coupon the coupon to register
	 */
	public void registerCoupon(Coupon coupon) {
		this.coupons.add(coupon);
	}
	
	/**
	 * Create a new coupon with the specified values and register it
	 * 
	 * @param code the code for the coupon
	 * @param rewards the rewards for the coupon
	 * 
	 * @return the new coupon that was created
	 */
	public Coupon createCoupon(String code, ItemStack... rewards) {
		if (this.couponExists(code)) throw new IllegalArgumentException("A coupon with the code " + code + " already exists");
		
		Coupon coupon = new Coupon(code, rewards);
		this.coupons.add(coupon);
		return coupon;
	}
	
	/**
	 * Delete a coupon from the registry
	 * 
	 * @param coupon the coupon to delete
	 * @return true if successfully deleted, false otherwise
	 */
	public boolean deleteCoupon(Coupon coupon) {
		return this.coupons.remove(coupon);
	}
	
	/**
	 * Delete a coupon from the registry with the specified code
	 * 
	 * @param code the code to delete
	 * @return true if successfully deleted, false otherwise
	 */
	public boolean deleteCoupon(String code) {
		return this.coupons.removeIf(c -> c.getCode().equals(code));
	}
	
	/**
	 * Get an instance of a coupon object based on its code
	 * 
	 * @param code the coupon code
	 * @return an instance of the coupon, or null if none found
	 */
	public Coupon getCoupon(String code) {
		return this.coupons.stream().filter(c -> c.getCode().equals(code)).findFirst().orElse(null);
	}
	
	/**
	 * Check whether a coupon with the given code is registered or not
	 * 
	 * @param code the coupon code to check
	 * @return true if the coupon exists, false otherwise
	 */
	public boolean couponExists(String code) {
		return this.coupons.stream().anyMatch(c -> c.getCode().equals(code));
	}
	
	/**
	 * Get an immutable set of all registered coupons
	 * 
	 * @return a set of all coupons
	 */
	public Set<Coupon> getCoupons() {
		return ImmutableSet.copyOf(coupons);
	}
	
	/**
	 * Clear all locally stored data in the coupon registry
	 */
	public void clearCouponData() {
		this.coupons.forEach(c -> c.clearData());
		this.coupons.clear();
	}
}