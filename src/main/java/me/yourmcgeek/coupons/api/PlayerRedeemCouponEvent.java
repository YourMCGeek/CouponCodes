package me.yourmcgeek.coupons.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import me.yourmcgeek.coupons.coupon.Coupon;

/**
 * Called when a player successfully redeems a coupon using the "/coupon redeem"
 * command. If this event is cancelled, it will not be considered a valid redemption
 * and the player will not receive their rewards (i.e. they may attempt to redeem
 * the coupon another time)
 * 
 * @author Parker Hawke - 2008Choco
 */
public class PlayerRedeemCouponEvent extends PlayerEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	private boolean cancelled = false;
	
	private final Coupon coupon;
	
	/**
	 * Construct a new PlayerRedeemCouponEvent
	 * 
	 * @param player the player that redeemed the coupon
	 * @param coupon the coupon that was redeemed
	 */
	public PlayerRedeemCouponEvent(Player player, Coupon coupon) {
		super(player);
		this.coupon = coupon;
	}
	
	/**
	 * Get the coupon that is to be redeemed
	 * 
	 * @return the coupon to be redeemed
	 */
	public Coupon getCoupon() {
		return coupon;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}