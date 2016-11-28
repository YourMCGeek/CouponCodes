package me.yourmcgeek.coupons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/** Represents a coupon registered to the server, its code and its rewards
 * @author Parker Hawke - 2008Choco
 */
public class Coupon {
	
	private final List<UUID> redeemed = new ArrayList<>();
	
	private final Set<ItemStack> rewards = new HashSet<>();
	private final String code;
	
	public Coupon(String code, ItemStack... rewards) {
		this.code = code;
		this.rewards.addAll(Arrays.asList(rewards));
	}
	
	/** Get the code required to redeem this coupon
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/** Get an immutable list of all rewards from this coupon
	 * @return a list of all rewards
	 */
	public Set<ItemStack> getRewards() {
		return ImmutableSet.copyOf(rewards);
	}
	
	/** Add rewards to this coupon
	 * @param items - The items to add
	 */
	public void addRewards(ItemStack... items) {
		this.rewards.addAll(Arrays.asList(items));
	}
	
	/** Check whether a UUID has redeemed this coupon or not
	 * @param uuid - The UUID to check
	 * @return true if already redeemed
	 */
	public boolean hasRedeemed(UUID uuid) {
		return this.redeemed.contains(uuid);
	}
	
	/** Check whether a Player has redeemed this coupon or not
	 * @param player - The player to check
	 * @return true if already redeemed
	 */
	public boolean hasRedeemed(Player player) {
		return this.hasRedeemed(player.getUniqueId());
	}
	
	/** Set that a UUID has redeemed this coupon
	 * @param uuid - The UUID to flag as redeemed
	 */
	public void setRedeemed(UUID uuid) {
		this.redeemed.add(uuid);
	}
	
	/** Set that a Player has redeemed this coupon
	 * @param player - The player to flag as redeemed
	 */
	public void setRedeemed(Player player) {
		this.setRedeemed(player.getUniqueId());
	}
	
	/** Set that a UUID has not redeemed this coupon, and allow 
	 * the ability to redeem it again
	 * @param uuid - The UUID to allow to redeem again
	 */
	public void unredeem(UUID uuid) {
		this.redeemed.remove(uuid);
	}
	
	/** Set that a Player has not redeemed this coupon, and allow 
	 * the ability to redeem it again
	 * @param player - The player to allow to redeem again
	 */
	public void unredeem(Player player) {
		this.unredeem(player.getUniqueId());
	}
	
	/** Get am immutable list of all UUIDs that have redeemed this coupon
	 * @return a list of all redeemers
	 */
	public List<UUID> getRedeemed() {
		return ImmutableList.copyOf(this.redeemed);
	}
}