package me.yourmcgeek.coupons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/** Represents a coupon registered to the server, its code and its rewards
 * @author Parker Hawke - 2008Choco
 */
@SerializableAs("Coupon")
public class Coupon implements ConfigurationSerializable {
	
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
	
	/** Clear all data stored in the coupon object
	 */
	public void clearData() {
		this.rewards.clear();
		this.redeemed.clear();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		
		data.put("code", this.code);
		
		List<String> serializedItems = new ArrayList<>();
		for (ItemStack item : this.rewards)
			serializedItems.add(item.getType() + ":" + item.getData() + "|" + item.getAmount());
		data.put("rewards", serializedItems);
		
		List<String> redeemedUUIDS = this.redeemed.stream().distinct().map(UUID::toString).collect(Collectors.toList()); // Convert all UUIDs to Strings
		data.put("redeemed", redeemedUUIDS);
		return data;
	}
	
	private static final Pattern ITEM_PATTERN = Pattern.compile("(\\w+)(?:(?:\\:{1})(\\d+)){0,1}(?:(?:\\|{1})(\\d+)){0,1}");
	
	@SuppressWarnings("unchecked")
	public Coupon deserialize(Map<String, Object> data) {
		String code = "";
		List<UUID> redeemed = new ArrayList<>();
		
		if (data.containsKey("code")){
			code = (String) data.get("code");
		}
		else return null;

		Coupon coupon = new Coupon(code);
		
		if (data.containsKey("rewards")){
			String rewardString = String.join(",", (List<String>) data.get("rewards"));
			Matcher matcher = ITEM_PATTERN.matcher(rewardString);
			
			while (matcher.find()){
				String materialString = matcher.group(1).toUpperCase();
				String itemDataString = matcher.group(2);
				String itemCountString = matcher.group(3);
				
				Material material = Material.valueOf(materialString);
				byte itemData = NumberUtils.toByte(itemDataString);
				int itemCount = NumberUtils.toInt(itemCountString, 1);
				
				if (material == null) continue;
				
				ItemStack item = new ItemStack(material, itemCount, itemData);
				coupon.addRewards(item);
			}
		}
		
		if (data.containsKey("redeemed")){
			List<String> redeemedStrings = (List<String>) data.get("redeemed");
			for (String redeemedString : redeemedStrings)
				redeemed.add(UUID.fromString(redeemedString));
			
			coupon.redeemed.addAll(redeemed);
		}
		
		return coupon;
	}
}