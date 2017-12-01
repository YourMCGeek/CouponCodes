package me.yourmcgeek.coupons.coupon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import me.yourmcgeek.coupons.CouponCodes;

/**
 * Represents a coupon registered to the server, its code and its rewards
 * 
 * @author Parker Hawke - 2008Choco
 */
public class Coupon {
	
	private static final Consumer<Player> EMPTY_CONSUMER = p -> {};
	
	private boolean redeemable = true;
	private Consumer<Player> redeemAction = EMPTY_CONSUMER;
	
	private final Set<UUID> redeemed = new HashSet<>();
	private final List<ItemStack> rewards = new ArrayList<>();
	private final String code;
	
	/**
	 * Construct a new Coupon with a given code and ItemStack rewards
	 * 
	 * @param code the coupon's code
	 * @param rewards the rewards for this coupon
	 */
	public Coupon(String code, ItemStack... rewards) {
		this.code = code;
		this.addRewards(rewards);
	}
	
	/**
	 * Get the code required to redeem this coupon
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Get an immutable list of all rewards from this coupon
	 * 
	 * @return a list of all rewards
	 */
	public List<ItemStack> getRewards() {
		return ImmutableList.copyOf(rewards);
	}
	
	/**
	 * Add rewards to this coupon
	 * 
	 * @param items the items to add
	 */
	public void addRewards(ItemStack... items) {
		for (ItemStack item : items) {
			this.rewards.add(item);
		}
	}
	
	/**
	 * Check whether a UUID has redeemed this coupon or not
	 * 
	 * @param uuid the UUID to check
	 * @return true if already redeemed, false otherwise
	 */
	public boolean hasRedeemed(UUID uuid) {
		return this.redeemed.contains(uuid);
	}
	
	/**
	 * Check whether a Player has redeemed this coupon or not
	 * 
	 * @param player the player to check
	 * @return true if already redeemed, false otherwise
	 */
	public boolean hasRedeemed(Player player) {
		return this.hasRedeemed(player.getUniqueId());
	}
	
	/**
	 * Set that a UUID has redeemed this coupon
	 * 
	 * @param uuid the UUID to flag as redeemed
	 */
	public void redeem(UUID uuid) {
		this.redeemed.add(uuid);
	}
	
	/**
	 * Set that a Player has redeemed this coupon
	 * 
	 * @param player the player to flag as redeemed
	 */
	public void redeem(Player player) {
		this.redeem(player.getUniqueId());
	}
	
	/**
	 * Set that a UUID has not redeemed this coupon, and allow the ability to redeem it again
	 * 
	 * @param uuid the UUID to allow to redeem again
	 */
	public void unredeem(UUID uuid) {
		this.redeemed.remove(uuid);
	}
	
	/**
	 * Set that a Player has not redeemed this coupon, and allow the ability to redeem it again
	 * 
	 * @param player the player to allow to redeem again
	 */
	public void unredeem(Player player) {
		this.unredeem(player.getUniqueId());
	}
	
	/**
	 * Get am immutable set of all UUIDs that have redeemed this coupon
	 * 
	 * @return a list of all redeemers
	 */
	public Set<UUID> getRedeemed() {
		return ImmutableSet.copyOf(this.redeemed);
	}
	
	/**
	 * Set whether this coupon is capable of being redeemed by a player or not
	 * 
	 * @param redeemable the new redeemable state
	 */
	public void setRedeemable(boolean redeemable) {
		this.redeemable = redeemable;
	}
	
	/**
	 * Check whether this coupon is redeemable by a player
	 * 
	 * @return true if redeemable, false otherwise
	 */
	public boolean isRedeemable() {
		return redeemable;
	}
	
	/**
	 * Set the action to be called when this coupon code is redeemed given the
	 * Player that redeemed the code. Setting this to null will use the default,
	 * empty consumer with no action
	 * 
	 * @param redeemAction the new player redemption consumer action
	 */
	public void setRedeemAction(Consumer<Player> redeemAction) {
		this.redeemAction = (redeemAction != null) ? redeemAction : EMPTY_CONSUMER;
	}
	
	/**
	 * Get the action to be called when this coupon code is redeemed. null can
	 * never be returned. If no action has been set, an empty Consumer will be
	 * returned
	 * 
	 * @return the player redemption consumer action
	 */
	public Consumer<Player> getRedeemAction() {
		return redeemAction;
	}
	
	/**
	 * Clear all data stored in the coupon object
	 */
	public void clearData() {
		this.rewards.clear();
		this.redeemed.clear();
	}
	
	public static final class CouponSerializer implements JsonSerializer<Coupon> {
		
		@Override
		@SuppressWarnings("deprecation") // TODO Will have to fix this for 1.13
		public JsonElement serialize(Coupon coupon, Type type, JsonSerializationContext context) {
			JsonObject root = new JsonObject();
			
			root.addProperty("code", coupon.code);
			root.addProperty("redeemable", coupon.redeemable);
			
			if (!coupon.redeemed.isEmpty()) {
				JsonArray redeemedData = new JsonArray();
				for (UUID redeemer : coupon.redeemed) {
					redeemedData.add(redeemer.toString());
				}
				root.add("redeemed", redeemedData);
			}
			
			if (!coupon.rewards.isEmpty()) {
				JsonArray rewardData = new JsonArray();
				for (ItemStack reward : coupon.rewards) {
					rewardData.add(reward.getType() + ":" + reward.getData().getData() + "|" + reward.getAmount());
				}
				root.add("rewards", rewardData);
			}
			
			return root;
		}
		
	}
	
	public static final class CouponDeserializer implements JsonDeserializer<Coupon> {
		
		@Override
		public Coupon deserialize(JsonElement data, Type type, JsonDeserializationContext context) throws JsonParseException {
			if (!data.isJsonObject()) return null;
			
			JsonObject root = data.getAsJsonObject();
			String code = root.get("code").getAsString();
			
			Coupon coupon = new Coupon(code);
			coupon.redeemable = root.get("redeemable").getAsBoolean();
			
			// Redeemed UUIDs
			if (root.has("redeemed")) {
				JsonArray redeemedData = root.getAsJsonArray("redeemed");
				for (JsonElement redeemer : redeemedData) {
					coupon.redeemed.add(UUID.fromString(redeemer.getAsString()));
				}
			}
			
			// Parsing loot data
			if (root.has("rewards")) {
				JsonArray rewardData = root.getAsJsonArray("rewards");
				for (JsonElement reward : rewardData) {
					Matcher matcher = CouponCodes.ITEM_PATTERN.matcher(reward.getAsString());
					
					if (matcher.find()) {
						String materialString = matcher.group(1).toUpperCase();
						String itemDataString = matcher.group(2);
						String itemCountString = matcher.group(3);
						
						@SuppressWarnings("deprecation")
						Material material = NumberUtils.isNumber(materialString)
								? Material.getMaterial(Integer.valueOf(materialString))
								: Material.getMaterial(materialString);
						byte itemData = NumberUtils.toByte(itemDataString);
						int itemCount = NumberUtils.toInt(itemCountString, 1);
						
						if (material == null) continue;
						
						ItemStack item = new ItemStack(material, itemCount, itemData);
						coupon.addRewards(item);
					}
				}
			}
			
			return coupon;
		}
		
	}
	
}