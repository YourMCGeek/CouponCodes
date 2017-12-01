package me.yourmcgeek.coupons.utils;

import static org.bukkit.ChatColor.*;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.yourmcgeek.coupons.CouponCodes;

public final class BookUtils {
	
	private static final String TITLE = "Coupons Help Book", AUTHORS = "YourMCGeek & 2008Choco";
	private static final String DISPLAY_NAME = "Coupons Help Book", COLOR_DISPLAY_NAME = DARK_AQUA.toString() + BOLD + "Coupons Help Book";
	private static final List<String> LORE = Arrays.asList("A nice little guide to CouponCodes!");
	
	private BookUtils() {}
	
	public static ItemStack generateBook(CouponCodes plugin) {
		if (plugin.getInfoBook() != null) return plugin.getInfoBook();
		
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		
		// Book information
		bookMeta.setTitle(TITLE);
		bookMeta.setAuthor(AUTHORS);
		bookMeta.setLore(LORE);
		bookMeta.setDisplayName(plugin.getConfig().getBoolean("ColoredDisplayName") ? COLOR_DISPLAY_NAME : DISPLAY_NAME);
		
		// Page generation
		bookMeta.addPage(
			// Page 1
			DARK_GREEN + BOLD.toString() + UNDERLINE + "Welcome to Coupons!"
			+ "\n\n" + RESET + DARK_AQUA + "To create a coupon, use "
			+ "\n" + DARK_GREEN + "/coupons create {code} <material>[:data][;amount]" 
			+ "\n\n" + DARK_AQUA + "For example: " + DARK_GREEN + "/coupons create example_code dirt;64",
			
			// Page 2
			DARK_BLUE + "To view the list of all coupons, you can run the following command:" 
			+ "\n" + DARK_AQUA + "/coupons list"
			+ "\n\n" + DARK_BLUE + "Type it in chat to see all loaded coupons!" ,
			
			// Page 3
			DARK_PURPLE + "To redeem a coupon, run the command"
			+ "\n" + LIGHT_PURPLE + "/coupon redeem {code}"
			+ "\n\n" + DARK_PURPLE + "Let's redeem our coupon by running the following command"
			+ "\n" + LIGHT_PURPLE + "/coupon redeem example_code",
			
			// Page 4
			DARK_BLUE + "Since we no longer need our coupon, let's go ahead and delete it. To delete a coupon, run the command "
			+ "\n" + BLUE + "/coupons delete {code}"
			+ "\n\n" + DARK_BLUE + "To delete our coupon let's execute"
			+ "\n" + BLUE + "/coupons delete example_code",
			
			// Page 5
			DARK_RED + "Other Coupon commands include"
			+ "\n" + RED + "/coupons help"
			+ "\n/coupons book"
			+ "\n\n" + DARK_RED + "Make sure to check " + RED + "/coupons help" + DARK_RED + " as it will always be updated with the latest commands.",
			
			// Page 6
			"\n\n" + GOLD + MAGIC + "1234567891234567890"
			+ "\n\n" + DARK_GRAY + "   Happy Discounting!"
			+ "\n~YourMCGeek and 2008Choco"
			+ "\n\n" + GOLD + MAGIC + "1234567891234567890"
		);
		
		book.setItemMeta(bookMeta);
		return book;
	}
}