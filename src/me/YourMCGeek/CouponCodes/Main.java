package me.YourMCGeek.CouponCodes;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Tucker on 11/26/2016.
 */
public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		this.getLogger().info("CouponCodes is ready to provide discounts!");

		this.getCommand("create").setExecutor(new CommandCreate());
	}

	@Override
	public void onDisable() {
		this.getLogger().info("CouponCodes has run out of discounts!");
	}
}