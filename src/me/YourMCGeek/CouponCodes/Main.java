package me.YourMCGeek.CouponCodes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Tucker on 11/26/2016.
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getServer().getLogger().info("§3CouponCodes is ready to provide discounts!");
        this.getCommand("create").setExecutor(new CommandCreate());
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getLogger().info("§4CouponCodes has run out of discounts!");
    }
}
